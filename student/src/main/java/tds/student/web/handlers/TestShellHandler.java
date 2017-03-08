/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import AIR.Common.TDSLogger.ITDSLogger;
import AIR.Common.Web.Session.HttpContext;
import AIR.Common.Web.TDSReplyCode;
import AIR.Common.data.ResponseData;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.TDSSecurityException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import tds.blackbox.web.handlers.TDSHandler;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.AccProperties;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.abstractions.PrintService;
import tds.student.services.data.ApprovalInfo;
import tds.student.services.data.ApprovalInfo.OpportunityApprovalStatus;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.abstractions.IOpportunityRepository;
import tds.student.sql.abstractions.IResponseRepository;
import tds.student.sql.data.ClientLatency;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityStatus;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.TestSegment.TestSegmentApproval;
import tds.student.sql.data.Testee;
import tds.student.sql.data.ToolUsed;
import tds.student.tdslogger.StudentEventLogger;
import tds.student.web.StudentContext;
import tds.student.web.TestManager;
import tds.student.web.data.TestShellAudit;

import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.TDSSecurityException;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.UUID;

/**
 * @author mpatel
 * 
 */
@Controller
@Scope ("prototype")
public class TestShellHandler extends TDSHandler
{

  private static final Logger    _logger = LoggerFactory.getLogger (TestShellHandler.class);
  @Autowired
  private IOpportunityRepository _oppRepository;
  @Autowired
  @Qualifier("integrationOpportunityService")
  private IOpportunityService    _oppService;
  @Autowired
  @Qualifier("integrationResponseService")
  private IResponseService       _responseService;
  @Autowired
  private IResponseRepository    _responseRepository;
  @Autowired
  @Qualifier("integrationPrintService")
  private PrintService _printService;
  @Autowired
  private ITDSLogger             _tdsLogger;

  @RequestMapping (value = "TestShell.axd/logAuditTrail")
  @ResponseBody
  public ResponseData<String> logAuditTrail (HttpServletRequest request, HttpServletResponse response) {
    try {
      // TODO Shiva: Do we have this functionality? This needs to be
      // implemented.
      // There is parseOutLatencies() originally used in completeTest().

    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (String.format ("Problem in logAuditTrail. Message: %s", exp.getMessage ()));
    }
    return new ResponseData<String> (TDSReplyCode.OK.getCode (), "OK", "Logged");
  }

  @RequestMapping (value = "TestShell.axd/print")
  @ResponseBody
  public void Print (@RequestParam (value = "type", required = false) String type, @RequestParam (value = "page", required = false) int page,
      @RequestParam (value = "pageKey", required = false) UUID pageKey,
      @RequestParam (value = "params", required = false) String requestParams,
      @RequestParam (value = "position", required = false) final Integer position) throws TDSSecurityException,
      ReturnStatusException {
    checkAuthenticated ();

    // get opportunity
    TestOpportunity testOpp = StudentContext.getTestOpportunity ();
    AccLookup accLookup = StudentContext.getAccLookup ();
    AccProperties accProps = new AccProperties (accLookup);

    // get item group for this page
    PageGroup pageToPrint = _responseService.getItemGroup (testOpp.getOppInstance (), page, null, null, true);

    if (type.equals ("passage")) {
      if (accProps.isBrailleEnabled ()) {
        _printService.printPassageBraille (testOpp, pageToPrint, accLookup);
      } else {
        _printService.printPassage (testOpp.getOppInstance (), pageToPrint, requestParams);
      }

    } else if (type.equals ("page"))
    {
      if (accProps.isBrailleEnabled ())
      {
        _printService.printPageBraille (testOpp, pageToPrint, accLookup);
      }
      else
      {
        _printService.printPage (testOpp.getOppInstance (), pageToPrint, requestParams);
      }

    }
    else if (type.equals ("item")) {
      // find the item
      ItemResponse itemToPrint = (ItemResponse) CollectionUtils.find (pageToPrint, new Predicate ()
      {
        @Override
        public boolean evaluate (Object object) {
          ItemResponse response = (ItemResponse) object;
          return position == response.getPosition ();
        }
      });

      if (accProps.isBrailleEnabled ()) {
        _printService.printItemBraille (testOpp, itemToPrint, accLookup);
      } else {
        _printService.printItem (testOpp.getOppInstance (), itemToPrint, requestParams);
      }
    }
  }

  private String parseOutLatencies (Map<String, String> formParams) {
    Set<String> keys = formParams.keySet ();
    String latencies = null;
    for (String key : keys) {

      if (key.startsWith ("{\"latencies\":")) {
        latencies = key;
        break;
      }
    }
    return latencies;
  }

  @RequestMapping (value = "TestShell.axd/pauseTest")
  @ResponseBody
  public ResponseData<String> pauseTestNew (@RequestParam Map<String, String> formParams, @RequestParam (value = "reason", required = false) String reason, HttpServletRequest request)
      throws TDSSecurityException, ReturnStatusException {
    return PauseTest (formParams, reason, request);
  }

  @RequestMapping (value = "TestShell.axd/pause")
  @ResponseBody
  public ResponseData<String> PauseTest (@RequestParam Map<String, String> formParams, @RequestParam (value = "reason", required = false) String reason, HttpServletRequest request)
      throws TDSSecurityException, ReturnStatusException {
    // check if authenticated
    StudentEventLogger.info(StudentEventLogger.LogEvent.PAUSE_EXAM, StudentEventLogger.Checkpoint.ENTER.name(),
      request.getSession().getId(), null, null);
    if (isAuthenticated ()) {
      TestOpportunity testOpp = StudentContext.getTestOpportunity ();
      // only pause test if test opp exists (otherwise it doesn't matter)
      if (testOpp != null) {
        String latencies = parseOutLatencies (formParams);
        if (latencies != null) {
          TestShellAudit testShellAudit = null;
          try {
            ObjectMapper mapper = new ObjectMapper ();
            testShellAudit = mapper.readValue (latencies, TestShellAudit.class);
          } catch (IOException e) {
            StudentEventLogger.error( StudentEventLogger.LogEvent.PAUSE_EXAM, null,
              request.getSession().getId(), "Problem mapping pause request to TestShellAudit", null, e);
            _logger.error (String.format ("Problem mapping pause request to TestShellAudit: %s", e.getMessage ()));
          }
          PerformTestShellAudit (testOpp, testShellAudit, request);
        }
        StudentEventLogger.info(StudentEventLogger.LogEvent.PAUSE_EXAM, "pre-OpStatChg",
          request.getSession().getId(), null, null);
        // change status of opp to paused
        OpportunityStatusChange statusChange = new OpportunityStatusChange (OpportunityStatusType.Paused, true, reason);
        _oppService.setStatus (testOpp.getOppInstance (), statusChange);
        StudentEventLogger.info(StudentEventLogger.LogEvent.PAUSE_EXAM, "post-OpStatChg",
          request.getSession().getId(), null, null);
      }
    }
    // success
    StudentEventLogger.info(StudentEventLogger.LogEvent.PAUSE_EXAM, StudentEventLogger.Checkpoint.EXIT.name(),
      request.getSession().getId(), null, null);
    return new ResponseData<String> (TDSReplyCode.OK.getCode (), "OK", null);
  }

  @RequestMapping (value = "TestShell.axd/completeTest")
  @ResponseBody
  public ResponseData<String> reviewTestNew (@RequestParam Map<String, String> formParams, HttpServletRequest request) throws IOException, TDSSecurityException, ReturnStatusException {
    return reviewTest (formParams, request);
  }

  @RequestMapping (value = "TestShell.axd/complete")
  @ResponseBody
  public ResponseData<String> reviewTest (@RequestParam Map<String, String> formParams, HttpServletRequest request) throws IOException, TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();

    TestOpportunity testOpp = StudentContext.getTestOpportunity ();

    // get responses
    TestManager testManager = new TestManager (testOpp);
    testManager.LoadResponses (true);

    // check if test length is met
    testManager.CheckIfTestComplete ();

    if (!testManager.IsTestLengthMet ()) {
      // TODO mpatel - Check to see status and message in response and make sure
      // following works
      String message = "Review Test: Cannot end test because test length is not met.";
      _tdsLogger.applicationError (message, "reviewTest", request, null);
      HttpContext.getCurrentContext ().getResponse ().sendError (HttpStatus.SC_FORBIDDEN, "Cannot end the test.");
      return null;
    }

    // check if all visible pages are completed
    PageList pageList = testManager.GetVisiblePages ();

    if (!pageList.isAllCompleted ()) {
      String message = "Review Test: Cannot end test because all the groups have not been answered.";
      _tdsLogger.applicationError (message, "reviewTest", request, null);
      HttpContext.getCurrentContext ().getResponse ().sendError (HttpStatus.SC_FORBIDDEN, "Cannot end the test.");
      return null;
    }

    String latencies = parseOutLatencies (formParams);
    if (latencies != null) {
      TestShellAudit testShellAudit = null;
      try {
        ObjectMapper mapper = new ObjectMapper ();
        testShellAudit = mapper.readValue (latencies, TestShellAudit.class);
      } catch (IOException e) {
        _logger.error (String.format ("Problem mapping pause request to TestShellAudit: %s", e.getMessage ()));
      }
      PerformTestShellAudit (testOpp, testShellAudit, request);
    }
    // put test in review mode
    OpportunityStatusChange statusChange = new OpportunityStatusChange (OpportunityStatusType.Review, true);
    _oppService.setStatus (testOpp.getOppInstance (), statusChange);

    // success
    return new ResponseData<String> (TDSReplyCode.OK.getCode (), "OK", null);
  }

  /**
   * Gets the test shell audit json and logs data to the DB.
   * 
   * @param testOpp
   * @throws ReturnStatusException
   */
  private void PerformTestShellAudit (TestOpportunity testOpp, TestShellAudit testShellAudit, HttpServletRequest request) throws TDSSecurityException, ReturnStatusException {
    try {

      if (testShellAudit == null)
        return;
      // check if have latencies to log
      if (testShellAudit.getLatencies () != null && testShellAudit.getLatencies ().size () > 0) {
        LogClientLatencies (testOpp, testShellAudit.getLatencies (), request);
      }

      // check if have tool usage to log
      if (testShellAudit.getToolsUsed () != null && testShellAudit.getToolsUsed ().size () > 0) {
        LogToolsUsed (testOpp, testShellAudit.getToolsUsed ());
      }
    } catch (Exception ex) {
      _tdsLogger.applicationError (ex.getMessage (), "PerformTestShellAudit", request, ex);
    }

  }

  private void LogClientLatencies (TestOpportunity testOpp, List<ClientLatency> clientLatencies, HttpServletRequest request) {
    StringBuilder errorBuilder = new StringBuilder ();

    // look for errors
    for (ClientLatency clientLatency : clientLatencies) {
      List<String> latencyErrors = clientLatency.getErrors ();

      // log any latency validation errors
      if (latencyErrors != null && latencyErrors.size () > 0) {
        errorBuilder.append ("PAGE ").append (clientLatency.getItemPage ()).append (" ERRORS:");

        for (String error : latencyErrors) {
          errorBuilder.append ("* ");
          errorBuilder.append (error);
          errorBuilder.append (System.lineSeparator ());
        }
        errorBuilder.append (System.lineSeparator ());
      }
    }

    // write latency to DB
    try {
      _oppRepository.recordClientLatencies (testOpp.getOppInstance (), clientLatencies);
    } catch (Exception ex) {
      // log any exceptions
      errorBuilder.append ("EXCEPTION: ").append (ex);
      errorBuilder.append (System.lineSeparator ());
      errorBuilder.append (System.lineSeparator ());
    }

    // write error to DB
    if (errorBuilder.length () > 0) {
      String message = String.format ("Client latency exception/errors have occured: %s", errorBuilder.toString ());
      _tdsLogger.applicationError (message, "LogClientLatencies", request, null);
    }
  }

  private void LogToolsUsed (TestOpportunity testOpp, List<ToolUsed> toolsUsed) {
    try {
      _oppRepository.recordToolsUsed (testOpp.getOppInstance ().getKey (), toolsUsed);
    } catch (Exception e) {
      _logger.error (String.format ("Tools Used exception occured %s", e.getMessage ()));
    }
  }

  @RequestMapping (value = "TestShell.axd/waitForSegmentApproval")
  @ResponseBody
  private ResponseData<String> WaitForSegmentApproval (@RequestParam (value = "position", required = false) int segmentPosition,
      @RequestParam (value = "approval", required = false) String segmentApproval) throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();
    OpportunityInstance oppInstance = StudentContext.getOppInstance ();

    if (segmentApproval.equals ("entry")) {
      _oppService.waitForSegment (oppInstance, segmentPosition, TestSegmentApproval.Entry);
    } else if (segmentApproval.equals ("exit")) {
      _oppService.waitForSegment (oppInstance, segmentPosition, TestSegmentApproval.Exit);
    }

    return new ResponseData<String> (TDSReplyCode.OK.getCode (), "OK", null);
  }

  @RequestMapping (value = "TestShell.axd/checkForSegmentApproval")
  @ResponseBody
  private ResponseData<ApprovalInfo> CheckSegmentApproval () throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();
    OpportunityInstance oppInstance = StudentContext.getOppInstance ();

    ApprovalInfo approvalInfo = _oppService.checkSegmentApproval (oppInstance);
    if (approvalInfo.getComment () != null)
      approvalInfo.setComment (HtmlUtils.htmlEscape (approvalInfo.getComment ()));

    /*
     * if (approvalInfo.Status == OpportunityApprovalStatus.Denied) { throw new
     * ReturnStatusException(new
     * ReturnStatus(OpportunityApprovalStatus.Denied.ToString(),
     * approvalInfo.Comment)); }
     */

    if (approvalInfo.getStatus ().equals (OpportunityApprovalStatus.Logout)) {
      throw new ReturnStatusException (new ReturnStatus (OpportunityApprovalStatus.Logout.toString (), "Proctor logged out"));
    }

    return new ResponseData<ApprovalInfo> (TDSReplyCode.OK.getCode (), "OK", approvalInfo);
  }

  @RequestMapping (value = "TestShell.axd/exitSegment")
  @ResponseBody
  private void ExitSegment (@RequestParam (value = "position", required = false) int segmentPosition) throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();

    OpportunityInstance oppInstance = StudentContext.getOppInstance ();

    _oppRepository.exitSegment (oppInstance, segmentPosition);
  }

  @RequestMapping (value = "TestShell.axd/recordItemComment")
  @ResponseBody
  private ResponseData<String> RecordItemComment (@RequestParam (value = "position", required = false) int position, @RequestParam (value = "comment", required = false) String comment)
      throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();
    OpportunityInstance oppInstance = StudentContext.getOppInstance ();
    Testee testee = StudentContext.getTestee ();

    // encode and trim comment
    if (comment != null) {
      // TODO mpatel - Talk to Shiva and confirm we can replace following line
      // of code with UrlEncoderDecoderUtils
      // comment = AntiXss.HtmlEncode(comment, 2000);
      comment = HtmlUtils.htmlEscape (comment);
    }

    _responseRepository.recordComment (oppInstance.getSessionKey (), testee.getKey (), oppInstance.getKey (), position, comment);

    return new ResponseData<String> (TDSReplyCode.OK.getCode (), "OK", null);
  }

  @RequestMapping (value = "TestShell.axd/recordOppComment")
  @ResponseBody
  private ResponseData<String> RecordOppComment (@RequestParam (value = "comment", required = false) String comment) throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();
    OpportunityInstance oppInstance = StudentContext.getOppInstance ();
    Testee testee = StudentContext.getTestee ();

    // Following code commented in 2013 Dotnet version
    /*
     * //encode and trim comment if (comment != null) { // comment =
     * AntiXss.HtmlEncode(comment, 2000); comment =
     * UrlEncoderDecoderUtils.encode (comment); }
     */

    _oppRepository.recordComment (oppInstance.getSessionKey (), testee.getKey (), oppInstance.getKey (), comment);

    return new ResponseData<String> (TDSReplyCode.OK.getCode (), "OK", null);
  }

  @RequestMapping (value = "TestShell.axd/getOppComment")
  @ResponseBody
  private ResponseData<String> GetOppComment () throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();
    OpportunityInstance oppInstance = StudentContext.getOppInstance ();

    String comment = _oppRepository.getComment (oppInstance.getKey ());

    return new ResponseData<String> (TDSReplyCode.OK.getCode (), "OK", comment);
  }

  @RequestMapping (value = "TestShell.axd/markForReview")
  @ResponseBody
  private ResponseData<String> MarkForReview (@RequestParam (value = "position", required = false) int position, @RequestParam (value = "mark", required = false) boolean mark)
      throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();

    OpportunityInstance oppInstance = StudentContext.getOppInstance ();

    _responseRepository.setItemMarkForReview (oppInstance, position, mark);

    return new ResponseData<String> (TDSReplyCode.OK.getCode (), "OK", null);
  }

  @RequestMapping (value = "TestShell.axd/removeResponse")
  @ResponseBody
  private ResponseData<String> RemoveResponse (@RequestParam (value = "position", required = false) int position, @RequestParam (value = "itemID", required = false) String itemID,
      @RequestParam (value = "dateCreated", required = false) String dateCreated) throws TDSSecurityException, ReturnStatusException {
    OpportunityInstance oppInstance = StudentContext.getOppInstance ();

    _responseService.removeResponse (oppInstance, position, itemID, dateCreated);

    // success
    return new ResponseData<String> (TDSReplyCode.OK.getCode (), "OK", null);
  }

  // / <summary>
  // / Returns the opp status code.
  // / </summary>
  @RequestMapping (value = "TestShell.axd/getStatus")
  @ResponseBody
  private ResponseData<OpportunityStatusType> GetStatus () throws TDSSecurityException, ReturnStatusException {
    OpportunityStatus oppStatus = _oppService.getStatus (StudentContext.getOppInstance ());
    return new ResponseData<OpportunityStatusType> (TDSReplyCode.OK.getCode (), "OK", oppStatus.getStatus ());
  }

  @RequestMapping (value = "TestShell.axd/timer")
  @ResponseBody
  private void timerBatch (HttpServletRequest request, HttpServletResponse response) throws Exception {

  }

  @RequestMapping (value = "TestShell.axd/audit")
  @ResponseBody
  private ResponseData<String> audit (TestShellAudit testShellAudit) throws TDSSecurityException, ReturnStatusException {

    return new ResponseData<String> (TDSReplyCode.OK.getCode (), "OK", "Logged");
  }

}
