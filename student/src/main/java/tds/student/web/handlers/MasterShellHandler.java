/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.opentestsystem.shared.trapi.data.TestStatus;
import org.opentestsystem.shared.trapi.data.TestStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.AccProperties;
import tds.student.proxy.data.Proctor;
import tds.student.services.abstractions.IAccommodationsService;
import tds.student.services.abstractions.ILoginService;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.abstractions.IStudentPackageService;
import tds.student.services.abstractions.ITestScoringService;
import tds.student.services.data.ApprovalInfo;
import tds.student.services.data.ApprovalInfo.OpportunityApprovalStatus;
import tds.student.services.data.LoginInfo;
import tds.student.services.data.LoginKeyValues;
import tds.student.services.data.PageList;
import tds.student.services.data.TestOpportunity;
import tds.student.services.data.TestScoreStatus;
import tds.student.sql.abstractions.IOpportunityRepository;
import tds.student.sql.abstractions.IScoringRepository;
import tds.student.sql.data.Accommodations;
import tds.student.sql.data.BrowserCapabilities;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.ServerLatency;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestDisplayScores;
import tds.student.sql.data.TestForm;
import tds.student.sql.data.TestSelection;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.TestSummary;
import tds.student.sql.data.Testee;
import tds.student.web.BrowserInfoCookie;
import tds.student.web.DebugSettings;
import tds.student.web.ProxyContext;
import tds.student.web.StudentContext;
import tds.student.web.StudentContextException;
import tds.student.web.StudentCookie;
import tds.student.web.StudentSettings;
import tds.student.web.TestManager;
import AIR.Common.TDSLogger.ITDSLogger;
import AIR.Common.Web.BrowserParser;
import AIR.Common.Web.TDSReplyCode;
import AIR.Common.Web.WebHelper;
import AIR.Common.Web.Session.HttpContext;
import AIR.Common.data.ResponseData;
import AIR.Common.time.DateTime;
import TDS.Shared.Browser.BrowserInfo;
import TDS.Shared.Exceptions.FailedReturnStatusException;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.TDSSecurityException;

@Controller
@Scope ("prototype")
public class MasterShellHandler extends TDSHandler
{
  private static final Logger    _logger = LoggerFactory.getLogger (MasterShellHandler.class);

  @Autowired
  private IOpportunityService    _oppService;

  @Autowired
  private IAccommodationsService _accsService;

  @Autowired
  private ITestScoringService    _testScoringService;

  @Autowired
  private IOpportunityRepository _oppRepository;

  @Autowired
  private IResponseService       _responseService;

  @Autowired
  private StudentSettings        _studentSettings;

  @Autowired
  private ILoginService          _loginService;
  
  @Autowired
  private IScoringRepository     _scoringRepository;
  
  @Autowired
  private IStudentPackageService _studentPackageService;
  
  @Autowired
  private ITDSLogger             _tdsLogger;

  /***
   * 
   * @param sessionID
   * @param keyValues
   * @param forbiddenApps
   * @return
   * @throws ReturnStatusException
   * @throws FailedReturnStatusException
   * @throws IOException
   */
  @RequestMapping (value = "MasterShell.axd/loginStudent")
  @ResponseBody
  public ResponseData<LoginInfo> loginStudent (@RequestParam (value = "sessionID", required = false) String sessionID, @RequestParam (value = "keyValues", required = false) String keyValues,
      @RequestParam (value = "forbiddenApps", required = false) String forbiddenApps, HttpServletResponse response, HttpServletRequest request) throws ReturnStatusException, FailedReturnStatusException {
    long startTime = System.currentTimeMillis ();
    LoginInfo loginInfo;
    sessionID = sessionID != null ? sessionID : "";
    keyValues = keyValues != null ? keyValues : "";

    // get proctor session ID
    if (_studentSettings.isProxyLogin ()) {
      Proctor proctor = ProxyContext.GetProctor ();
      TestSession testSession = proctor.GetSessionInfo ();
      if (testSession != null)
        sessionID = testSession.getId ();
    }

    // create login command
    LoginKeyValues loginKeyValues = new LoginKeyValues ();
    for (String keyValue : keyValues.split (";")) {
      String[] keyValuePieces = keyValue.split (":");
      if (keyValuePieces.length > 1) {
        String key = keyValuePieces[0];
        String value = keyValuePieces[1];
        loginKeyValues.put (key, value);
      }
    }

    try {
      loginInfo = _loginService.login (sessionID, loginKeyValues);
    } catch (Exception ex) {
      String message = String.format("LOGIN EXCEPTION: %s", ex.getMessage ());
      _tdsLogger.applicationError (message, "loginStudent", request, ex);
      
      // get login error message
      String loginErrorKey = "Login.Label.Error";
      response.setStatus (HttpStatus.SC_INTERNAL_SERVER_ERROR);
      return new ResponseData<LoginInfo> (TDSReplyCode.Error.getCode (), loginErrorKey, null);
    }

    // check forbidden apps if there are no validation errors
    if (loginInfo.getValidationErrors ().size () == 0) {
      // get forbidden apps
      List<String> forbiddenAppsList = parseForbiddenApps (forbiddenApps);

      // Following TO DO is from Dot net code
      // TODO: Check if school is excluded
      // check if any forbidden apps
      if (forbiddenAppsList.size () > 0 && !DebugSettings.ignoreForbiddenApps ()) {
        StringBuilder message = new StringBuilder ();
        message.append ("ForbiddenApps");
        message.append (" ");
        message.append (StringUtils.join (forbiddenAppsList.toArray (), ", "));

        loginInfo.getValidationErrors ().add (message.toString ());
      }
    }

    // check for errors
    if (loginInfo.getValidationErrors ().size () > 0) {
      // get error
      String error = loginInfo.getValidationErrors ().get (0);

      // log error
      error = (error != null) ? error : "Unknown";
      String message = String.format ("LOGIN: %s [Values: %s, Session: %s]", error, keyValues, sessionID);
      _tdsLogger.applicationWarn (message, "loginStudent", request, null);
      
      // send error to client
      response.setStatus (HttpStatus.SC_FORBIDDEN);
      return new ResponseData<LoginInfo> (TDSReplyCode.Denied.getCode (), error, null);
    }

    // save cookie
    StudentContext.saveTestee (loginInfo.getTestee ());
    StudentContext.saveSession (loginInfo.getSession ());
    StudentCookie.writeStore();
    _logger.info ("<<<<<<<<< loginStudent Total Execution Time : "+((System.currentTimeMillis ()-startTime)/1000) + " seconds");
    return new ResponseData<LoginInfo> (TDSReplyCode.OK.getCode (), "OK", loginInfo);
  }

  /***
   * Get all the eligible tests for the student.
   * 
   * @param grade
   * @return
   * @throws ReturnStatusException
   * @throws TDSSecurityException
   * @throws StudentContextException
   */
  @RequestMapping (value = "MasterShell.axd/getTests")
  @ResponseBody
  public ResponseData<List<TestSelection>> getTests (@RequestParam (value = "grade", required = false) String grade) throws ReturnStatusException, TDSSecurityException, StudentContextException {
    long startTime = System.currentTimeMillis ();
    List<TestSelection> testSelections = null;
    checkAuthenticated ();

    TestSession session = StudentContext.getSession ();
    Testee testee = StudentContext.getTestee ();

    // validate context
    if (session == null || testee == null) {
      StudentContext.throwMissingException ();
    }
    // if there was no grade passed in the use students grade
    if (StringUtils.isEmpty (grade)) {
      grade = testee.getGrade ();
    }
    // get all tests for this grade
    BrowserInfo browserInfo = null;
    if (!DebugSettings.ignoreBrowserChecks ())
    {
        browserInfo = BrowserInfo.GetHttpCurrent();
    }
    
    testSelections = _oppService.getEligibleTests (testee, session, grade,browserInfo);
    _logger.info ("<<<<<<<<< getTests Total Execution Time : "+((System.currentTimeMillis ()-startTime)/1000) + " seconds");
    return new ResponseData<List<TestSelection>> (TDSReplyCode.OK.getCode (), "OK", testSelections);
  }

  /***
   * For a proctorless session return all the accommodations for a test and
   * allow the student to choose from them.
   * 
   * @param testKey
   * @return
   * @throws ReturnStatusException
   * @throws TDSSecurityException
   */
  @RequestMapping (value = "MasterShell.axd/getSegmentsAccommodations")
  @ResponseBody
  public ResponseData<List<Accommodations>> getSegmentsAccommodations (@RequestParam (value = "testKey", required = false) String testKey) throws ReturnStatusException, TDSSecurityException {
    List<Accommodations> segmentAccsList = null;
    checkAuthenticated ();
    TestSession session = StudentContext.getSession ();
    Testee testee = StudentContext.getTestee ();
    // get test/segment accommodations
    segmentAccsList = _accsService.getTestee (testKey, isGuestSession (session), testee.getKey ());
    return new ResponseData<List<Accommodations>> (TDSReplyCode.OK.getCode (), "OK", segmentAccsList);
  }

  /**
   * 
   * @param testKey
   * @param testID
   * @param subject
   * @param grade
   * @param segment
   * @param oppKey
   * @return
   * @throws ReturnStatusException
   * @throws TDSSecurityException
   * @throws StudentContextException
   */
  // TODO shiva: under what circumstances segment parameter will be multiple
  // values ? If they are multiple values then
  @RequestMapping (value = "MasterShell.axd/openTest")
  @ResponseBody
  public ResponseData<OpportunityInfoJsonModel> openTest (@RequestParam (value = "testKey", required = false) String testKey, @RequestParam (value = "testID", required = false) String testID,
      @RequestParam (value = "subject", required = false) String subject, @RequestParam (value = "grade", required = false) String grade, @RequestParam (value = "oppKey", required = false) UUID oppKey)
      throws ReturnStatusException, TDSSecurityException, StudentContextException {
    OpportunityInfoJsonModel opportunityInfoJsonModel = new OpportunityInfoJsonModel ();
    checkAuthenticated ();
    // get test properties
    TestSession session = StudentContext.getSession ();
    Testee testee = StudentContext.getTestee ();
    
    // validate context
    if (session == null || testee == null) {
      StudentContext.throwMissingException ();
    }
    
    
    // create json response
    opportunityInfoJsonModel.setTestForms (new ArrayList<TestForm> ());
    opportunityInfoJsonModel.setTesteeForms (new ArrayList<String> ());
    OpportunityInfo oppInfo = null;

    oppInfo = _oppService.openTest (testee, session, testKey);
    OpportunityInstance oppInstance = oppInfo.createOpportunityInstance (session.getKey ());

    // if we are in PT mode and the session is proctorless then we need to
    // approve the accommodations
    if (_studentSettings.getInPTMode () && session.isProctorless ()) {
      // get segment postback data approve (for PT)
      List<String> segmentsData = WebHelper.getQueryValues ("segment");
      _accsService.approve (oppInstance, segmentsData);
    }

    // save cookie
    StudentContext.saveOppInfo (testKey, testID, oppInfo);
    StudentContext.saveSubject (subject);
    StudentContext.saveGrade (grade);
    StudentCookie.writeStore();
    return new ResponseData<OpportunityInfoJsonModel> (TDSReplyCode.OK.getCode (), "OK", opportunityInfoJsonModel);
  }

  /***
   * Check if a test is approved by the proctor and if it then return the
   * accommodations.
   * 
   * @return
   * @throws ReturnStatusException
   * @throws TDSSecurityException
   * @throws StudentContextException
   */
  @RequestMapping (value = "MasterShell.axd/checkApproval")
  @ResponseBody
  public ResponseData<ApprovalInfo> checkTestApproval () throws ReturnStatusException, TDSSecurityException, StudentContextException {
    // check if test is approved
    ApprovalInfo oppApproval = null;

    checkAuthenticated ();

    OpportunityInstance oppInstance = StudentContext.getOppInstance ();
    String testKey = StudentContext.getTestKey ();
    Testee testee = StudentContext.getTestee ();
    TestSession testSession = StudentContext.getSession ();

    // validate context
    if (testKey == null || testSession == null || testee == null || oppInstance == null) {
      StudentContext.throwMissingException ();
    }

    // check if the proctor has responded and get back accommodations if
    // student has been approved
    boolean isGuestSession = isGuestSession (testSession);
    oppApproval = _oppService.checkTestApproval (oppInstance);
    // clean up comment
    oppApproval.setComment (oppApproval.getComment ());
    // if the opportunity was approved and a testkey was provided load
    // accommodations
    if (oppApproval.getStatus () == OpportunityApprovalStatus.Approved) {
      // load the opportunity accommodations
      List<Accommodations> segmentsAccommodations = null;
      segmentsAccommodations = _accsService.getApproved (oppInstance, testKey, isGuestSession);
      oppApproval.setSegmentsAccommodations (segmentsAccommodations);
    }

    // if there are accommodations then proctor approved us
    if (oppApproval.getSegmentsAccommodations () != null) {
      // save cookie
      StudentContext.saveSegmentsAccommodations (oppApproval.getSegmentsAccommodations ());
      StudentCookie.writeStore();
    }
    return new ResponseData<ApprovalInfo> (TDSReplyCode.OK.getCode (), "OK", oppApproval);
  }

  /***
   * 
   * @return
   * @throws ReturnStatusException
   * @throws TDSSecurityException
   * @throws StudentContextException
   */
  @RequestMapping (value = "MasterShell.axd/denyApproval")
  @ResponseBody
  private ResponseData<Long> denyApproval () throws ReturnStatusException, TDSSecurityException, StudentContextException {
    checkAuthenticated ();
    OpportunityInstance oppInstance = StudentContext.getOppInstance ();
    // validate context
    if (oppInstance == null)
      StudentContext.throwMissingException ();
    // deny
    _oppService.denyApproval (oppInstance);

    return new ResponseData<Long> (TDSReplyCode.OK.getCode (), "OK", TDSReplyCode.OK.getCode ());
  }

  /***
   * 
   * @param formKey
   * @return
   * @throws ReturnStatusException
   * @throws TDSSecurityException
   * @throws StudentContextException
   */
  @RequestMapping (value = "MasterShell.axd/startTest")
  @ResponseBody
  private ResponseData<TestConfig> startTest (@RequestParam (value = "formKey", required = false) String formKey) throws ReturnStatusException, TDSSecurityException, StudentContextException {
    // try and start test
    TestConfig testConfig = null;

    checkAuthenticated ();

    // validate context
    OpportunityInstance oppInstance = StudentContext.getOppInstance ();
    if (oppInstance == null)
      StudentContext.throwMissingException ();

    // ge test key
    String testKey = StudentContext.getTestKey ();

    // get form key (provided in data entry mode only)
    List<String> formKeys = new ArrayList<String> ();
    if (!StringUtils.isEmpty (formKey))
      formKeys.add (formKey);

    testConfig = _oppService.startTest (oppInstance, testKey, formKeys);

    // save test config info
    StudentContext.saveTestConfig (testConfig);
    StudentCookie.writeStore();
    sendTestStatus(StudentContext.getTestee ().getId (), testKey, oppInstance.getKey (), TestStatusType.STARTED);
    // log browser info
    logBrowser (oppInstance, testConfig.getRestart ());
    
    return new ResponseData<TestConfig> (TDSReplyCode.OK.getCode (), "OK", testConfig);
  }

  /***
   * 
   * @return
   * @throws ReturnStatusException
   * @throws TDSSecurityException
   * @throws StudentContextException
   */
  @RequestMapping (value = "MasterShell.axd/pauseTest")
  @ResponseBody
  private ResponseData<Long> pauseTest () throws ReturnStatusException, TDSSecurityException, StudentContextException {
    checkAuthenticated ();
    OpportunityInstance oppInstance = StudentContext.getOppInstance ();
    // validate context
    if (oppInstance == null)
      StudentContext.throwMissingException ();
    _oppService.setStatus (oppInstance, new OpportunityStatusChange (OpportunityStatusType.Paused, false, null));
    return new ResponseData<Long> (TDSReplyCode.OK.getCode (), "OK", TDSReplyCode.OK.getCode ());
  }

  /***
   * 
   * @throws ReturnStatusException
   * @throws TDSSecurityException
   * @throws StudentContextException
   */
  @RequestMapping (value = "MasterShell.axd/scoreTest")
  @ResponseBody
  public ResponseData<TestSummary> scoreTest (HttpServletRequest request) throws ReturnStatusException, TDSSecurityException, StudentContextException {
    long startTime = System.currentTimeMillis ();
    checkAuthenticated ();

    TestOpportunity testOpp = StudentContext.getTestOpportunity ();

    // validate context
    if (testOpp == null)
      StudentContext.throwMissingException ();

    TestManager testManager = new TestManager (testOpp);
    testManager.LoadResponses (true);
    _logger.info ("<<<<<<<<< scoreTest Execution Time 1: "+((System.currentTimeMillis ()-startTime)) + " ms. ");
    // If there are more adaptive item groups to take then stop here and
    // return
    testManager.CheckIfTestComplete ();

    if (!testManager.IsTestLengthMet ()) {
      String message = "Review: A student has tried to complete the test but there are still more items left to be generated.";
       _tdsLogger.applicationError(message, "scoreTest", request, null);
      HttpContext.getCurrentContext ().getResponse ().setStatus (HttpStatus.SC_FORBIDDEN);
      return new ResponseData<TestSummary> (TDSReplyCode.Denied.getCode (), message, null);
    }
    _logger.info ("<<<<<<<<< scoreTest Execution Time 2: "+((System.currentTimeMillis ()-startTime)) + " ms. ");
    // check if all visible pages are completed
    PageList pages = testManager.GetVisiblePages ();

    if (!pages.isAllCompleted ()) {
      String message = "Review: A student has tried to complete the test but all the questions are not completed.";
      _tdsLogger.applicationError(message, "scoreTest", request, null);
      HttpContext.getCurrentContext ().getResponse ().setStatus (HttpStatus.SC_FORBIDDEN);
      return new ResponseData<TestSummary> (TDSReplyCode.Denied.getCode (), message, null);
    }
    _logger.info ("<<<<<<<<< scoreTest Execution Time 3: "+((System.currentTimeMillis ()-startTime)) + " ms. ");
    // complete test
    _oppService.setStatus (testOpp.getOppInstance (), new OpportunityStatusChange (OpportunityStatusType.Completed, true));
    
     sendTestStatus(StudentContext.getTestee ().getId(), testOpp.getTestKey (), testOpp.getOppInstance ().getKey (), TestStatusType.COMPLETED);

    // score the test
    TestScoreStatus scoreStatus = _testScoringService.scoreTest (testOpp.getOppInstance ().getKey (), testOpp.getTestKey ());
    _logger.info ("<<<<<<<<< scoreTest Execution Time 4: "+((System.currentTimeMillis ()-startTime)) + " ms. ");
    // if we successfully scored the record the server latency
    if (scoreStatus == TestScoreStatus.Submitted) {
      ServerLatency latency = ServerLatency.getCurrent (HttpContext.getCurrentContext ());
      latency.setOperation (ServerLatency.OperationType.Score);
    }

    // try and get scores
    ResponseData<TestSummary> testSummary =  getTestSummary ();
    _logger.info ("<<<<<<<<< scoreTest Total Execution Time : "+((System.currentTimeMillis ()-startTime)) + " ms. ");
    return testSummary;
  }

  /***
   * Gets the test summary (test and item scores) at the end of the test.
   * 
   * @return
   * @throws ReturnStatusException
   * @throws TDSSecurityException
   * @throws StudentContextException
   */
  @RequestMapping (value = "MasterShell.axd/getDisplayScores")
  @ResponseBody
  private ResponseData<TestSummary> getTestSummary () throws ReturnStatusException, TDSSecurityException, StudentContextException {
    TestSummary testSummary = new TestSummary ();
    checkAuthenticated ();
    // get test opp
    TestOpportunity testOpp = StudentContext.getTestOpportunity ();
    if (testOpp == null)
      StudentContext.throwMissingException ();
    // get accommodations
    AccLookup accLookup = StudentContext.getAccLookup ();
    AccProperties accProps = new AccProperties (accLookup);

    // if we are supressing scores then stop here
    if (accProps.isSuppressScore ()) {
      // WriteJsonReply(testSummary);
      return new ResponseData<TestSummary> (TDSReplyCode.OK.getCode (), "OK", testSummary);
    }

    UUID oppKey = testOpp.getOppInstance ().getKey ();
    // get test scores
    TestDisplayScores testDisplayScores = null;
    testDisplayScores = _scoringRepository.getDisplayScores (oppKey);

    boolean showTestScores = (testDisplayScores.isShowScores () && testDisplayScores.isScoreByTDS ());

    boolean showItemScores = (_studentSettings.getInPTMode () && (accProps.isItemScoreReportSummary () || accProps.isItemScoreReportResponses ()));

    // check if we are showing test scores
    if (showTestScores) {
      // check if the test is scored already
      if (testDisplayScores.isScored ()) {
        testSummary.setTestScores (testDisplayScores);
      }
      // otherwise wait for test to finish being scored
      else {
        testSummary.setPollForScores (true);
      }
    }

    // check if we are showing item scores
    if (showItemScores) {
      // check if the items are done scoring and we are not polling for test
      // scores
      if (testDisplayScores.isCompleted () && !testSummary.isPollForScores ()) {
        // get item scores
        _studentSettings.setShowItemScores (true);
        testSummary.setItemScores (_scoringRepository.getResponseRationales (testOpp.getOppInstance ()));
        testSummary.setViewResponses (accProps.isItemScoreReportResponses ());
      }
      // wait for items to finish scoring or polling for test scores to finish
      else {
        testSummary.setPollForScores (true);
      }
    }
    return new ResponseData<TestSummary> (TDSReplyCode.OK.getCode (), "OK", testSummary);
  }

  /**
   * 
   * @param forbiddenAppsFlat
   * @return
   * @throws ReturnStatusException
   */
  private static List<String> parseForbiddenApps (String forbiddenAppsFlat) throws ReturnStatusException {
    List<String> list = new ArrayList<String> ();
    List<String> forbiddenApps = null;
    // check if empty
    if (StringUtils.isEmpty (forbiddenAppsFlat))
      return list;

    String[] forbiddenAppsFlatListArray = forbiddenAppsFlat.split ("\\|");
    // parse string
    forbiddenApps = Arrays.asList (forbiddenAppsFlatListArray);
    Set<String> uniqueForbiddenApps = new HashSet<String> (forbiddenApps);
    // remove dups
    forbiddenApps = new ArrayList<String> (uniqueForbiddenApps);
    return forbiddenApps;
  }

  /***
   * Helper function for figuring out if we should disable guest session
   * accommodations.
   * 
   * @param session
   * @return boolean
   */
  private boolean isGuestSession (TestSession session) {
    return (_studentSettings.isProxyLogin () || session.isProctorless ());
  }

  /**
   * Logs browser information for this test opportunity
   * 
   * @param oppInstance
   * @param restart
   * @throws ReturnStatusException
   * @throws TDSSecurityException
   */
  private void logBrowser (OpportunityInstance oppInstance, int restart) throws ReturnStatusException, TDSSecurityException {
    try {
      HttpServletRequest httpRequest = getCurrentContext ().getRequest ();

      BrowserCapabilities browserCapabilities = new BrowserCapabilities ();
      browserCapabilities.setClientIP ((java.lang.String) httpRequest.getAttribute ("REMOTE_ADDR"));
      browserCapabilities.setProxyIP ((java.lang.String) httpRequest.getAttribute ("HTTP_X_FORWARDED_FOR"));

      // browserCapabilities.setClientIP
      // (httpRequest.ServerVariables["REMOTE_ADDR"]);
      // browserCapabilities.setProxyIP
      // (httpRequest.ServerVariables["HTTP_X_FORWARDED_FOR"];
      browserCapabilities.setUserAgent (httpRequest.getHeader ("User-Agent"));
      browserCapabilities.setMacAddress (BrowserInfoCookie.getMACAddress ());
      browserCapabilities.setLocalIP (BrowserInfoCookie.getLocalIP ());
      browserCapabilities.setScreenRez (BrowserInfoCookie.getScreen ());
      browserCapabilities.setTextToSpeech (BrowserInfoCookie.getTTS ());

      BrowserParser browser = new BrowserParser ();
      browserCapabilities.setIsSecure (browser.isSecureBrowser ());

      _oppRepository.logOpportunityClient (oppInstance, restart, browserCapabilities);
    } catch (ReturnStatusException re) {
      throw re;
    }
  }
  
  /**
   * Sends testStatus
   * 
   * @param testeeId
   * @param testKey
   * @param oppKey
   * @param testStatusType
   */
  private void sendTestStatus (String testeeId, String testKey, UUID oppKey, TestStatusType testStatusType) {
    try {
      if (testeeId.substring (0, 7).equalsIgnoreCase ("guest -")) {
         return;
       }
      TestStatus testStatus = new TestStatus ();
      testStatus.setStudentId (testeeId);
      testStatus.setTestId (testKey);
      testStatus.setOpportunity (_oppRepository.getOpportunityNumber(oppKey));
      testStatus.setUpdatedTime (DateTime.getFormattedDate (new Date (), "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
      testStatus.setStatus (testStatusType.name ());
      _studentPackageService.sendTestStatus(testStatus);
    } catch (Exception e) {
       _logger.error ("MasterShellHandler.sendTestStatus: "  + e.getMessage (), e);
    } 
  }
  
  
}
