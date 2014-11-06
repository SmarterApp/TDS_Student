/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.data.ApprovalInfo;
import tds.student.services.data.ApprovalInfo.OpportunityApprovalStatus;
import tds.student.sql.abstractions.IConfigRepository;
import tds.student.sql.abstractions.IItemBankRepository;
import tds.student.sql.abstractions.IOpportunityRepository;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunitySegment.OpportunitySegments;
import tds.student.sql.data.OpportunityStatus;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestSegment.TestSegmentApproval;
import tds.student.sql.data.TestSelection;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;
import AIR.Common.TDSLogger.ITDSLogger;
import TDS.Shared.Browser.BrowserAction;
import TDS.Shared.Browser.BrowserInfo;
import TDS.Shared.Browser.BrowserRule;
import TDS.Shared.Browser.BrowserValidation;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class OpportunityService implements IOpportunityService
{
  @Autowired
  private IItemBankRepository    _ibRepository;
  @Autowired
  private IOpportunityRepository _oppRepository;
  @Autowired
  private IConfigRepository		 _configRepository;
  @Autowired
  private ITDSLogger			 _tdsLogger;
  
  private static final Logger    _logger = LoggerFactory.getLogger (OpportunityService.class);

  public List<TestSelection> getEligibleTests (Testee testee, TestSession session, String grade, BrowserInfo browserInfo) throws ReturnStatusException {
    // get test availability info
    List<TestSelection> testSelectionList = null;
    try {

      testSelectionList = _oppRepository.getEligibleTests (testee.getKey (), session.getKey (), grade);
   // validate browser for this test
      if (browserInfo != null)
      {
          for (TestSelection testSelection:testSelectionList)
          {
              // check if test is available
              if (testSelection.getTestStatus () != TestSelection.Status.Disabled &&
                  testSelection.getTestStatus () != TestSelection.Status.Hidden)
              {
                  // build browser validator
                  BrowserValidation browserValidation = new BrowserValidation();
                  Iterable<BrowserRule> browserRules = _configRepository.getBrowserTestRules(testSelection.getTestID ());
                  for(BrowserRule browserRule:browserRules) {
                    browserValidation.AddRule (browserRule);
                  }

                  // check if any rules
                  if (browserValidation.GetRules()!=null && !browserValidation.GetRules().isEmpty ())
                  {
                      // get the rule that matches our current browser info
                      BrowserRule browserRule = browserValidation.FindRule(browserInfo);

                      // check if the browser is denied
                      if (browserRule == null || browserRule.getAction () == BrowserAction.Deny)
                      {
                          testSelection.setTestStatus(TestSelection.Status.Disabled);

                          if (browserRule == null || StringUtils.isEmpty (browserRule.getMessageKey ()))
                          {
                              testSelection.setReasonKey ("BrowserDeniedTest");
                          }
                          else
                          {
                              testSelection.setReasonKey (browserRule.getMessageKey ());
                          }
                      }
                      // check if we should show alert when clicking on test
                      else if (browserRule.getAction () == BrowserAction.Warn)
                      {
                          if (StringUtils.isEmpty (browserRule.getMessageKey ()))
                          {
                              testSelection.setWarningKey ( "BrowserWarnTest");
                          }
                          else
                          {
                              testSelection.setWarningKey(browserRule.getMessageKey());
                          }
                      }
                  }
              }
          }
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
    return testSelectionList;
  }

  // / <summary>
  // / Tries to open a test opportunity. The opportunity will need to be
  // approved before being able to start it.
  // / </summary>
  public OpportunityInfo openTest (Testee testee, TestSession session, String testKey) throws ReturnStatusException {
    OpportunityInfo oppConfig = null;
    try {
      // open test opp
      UUID browserKey = UUID.randomUUID ();
      OpportunityInfo sqlResult = _oppRepository.openTestOpportunity (testee.getKey (), testKey, session.getKey (), browserKey);
      // check if the opportunity was opened
      if (!sqlResult.getIsOpen ()) {
        // TODO
        // throw new ReturnStatusException (sqlResult.ReturnStatus);
        throw new ReturnStatusException (new ReturnStatus ("failed", "opportunity was opened"));
      }
      oppConfig = sqlResult;
      oppConfig.setBrowserKey (browserKey);
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return oppConfig;
  }

  // / <summary>
  // / Get the current opportunity status.
  // / </summary>
  public OpportunityStatus getStatus (OpportunityInstance oppInstance) throws ReturnStatusException {
    // get current opp status
    OpportunityStatus oppStatusResult = _oppRepository.validateAccess (oppInstance);
    try {
      // check if valid
      if (oppStatusResult.getReturnStatus () != null) {
        throw new ReturnStatusException (oppStatusResult.getReturnStatus ());
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      try {
    	  _logger.error (e.getReturnStatus().getAppKey());
      }
      catch (Exception e2) {}
      throw e;
    }
    return oppStatusResult;
  }

  // / <summary>
  // / Checks if the opportunity was approved by the proctor and if it was then
  // get the opportunity accommodations.
  // / </summary>
  public ApprovalInfo checkTestApproval (OpportunityInstance oppInstance) throws ReturnStatusException {
    try {
      // get current status
      return new ApprovalInfo (getStatus (oppInstance));
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      try {
    	  _logger.error (e.getReturnStatus().getAppKey());
      }
      catch (Exception e2) {}
      throw e;
    }
  }

  // / <summary>
  // / Checks if the segment was approved by the proctor and if it was then mark
  // opportunity status as "started".
  // / </summary>
  public ApprovalInfo checkSegmentApproval (OpportunityInstance oppInstance) throws ReturnStatusException {
    try {
      ApprovalInfo oppApprovalInfo = checkTestApproval (oppInstance);
      // if the segment was approved then set opportunity back to "started"
      if (oppApprovalInfo.getStatus () == OpportunityApprovalStatus.Approved) {
        setStatus (oppInstance, new OpportunityStatusChange (OpportunityStatusType.Started, true, "segment"));
      }
      return oppApprovalInfo;
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  public boolean setStatus (OpportunityInstance oppInstance, OpportunityStatusChange statusChange) throws ReturnStatusException {
    try {
      ReturnStatus returnedStatus = _oppRepository.setStatusWithValidation (oppInstance, statusChange.getStatus ().toString (), statusChange.getReason ());

      if (!statusChange.isCheckReturnStatus ())
        return true;

      // validate status
      if (returnedStatus != null && "failed".equalsIgnoreCase (returnedStatus.getStatus ())) {
        throw new ReturnStatusException (returnedStatus);
      }

      OpportunityStatusType newStatusType = OpportunityStatusType.parse (returnedStatus.getStatus ());

      // check to make sure could parse the SP's returned status
      if (newStatusType == OpportunityStatusType.Unknown) {
        String error = String.format ("WARNING: When parsing the returned status from SetStatus could not understand the returned value %s. Instead will use the passed in status of %s. This should be reviewed.",
        		returnedStatus.getStatus (), statusChange.getStatus ());
        _tdsLogger.applicationWarn(error, "setStatus", null, null);
        return false;
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return true;
  }

  // / <summary>
  // / When the proctor approves the test you can use this to deny the
  // accommodations that were set.
  // / </summary>
  public void denyApproval (OpportunityInstance oppInstance) throws ReturnStatusException {
    try {
      // get the current opp status
      OpportunityStatus oppStatus = getStatus (oppInstance);

      // BUG #24187: Test might already be paused..
      if (oppStatus.getStatus () == OpportunityStatusType.Paused)
        return;

      // If item count > 0 then the SP will set status to be 'suspended'
      setStatus (oppInstance, new OpportunityStatusChange (OpportunityStatusType.Pending, true, "denied"));
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  // / <summary>
  // / Use this to trigger starting the test.
  // / </summary>
  // / <returns>The test settings.</returns>
  public TestConfig startTest (OpportunityInstance oppInstance, String testKey, List<String> formKeys) throws ReturnStatusException {
    String formKeyList = null;
    TestConfig sqlResult = null;
    try {
      if (formKeys != null && formKeys.size () > 0) {
        formKeyList = formKeys.toString ();
        // formKeyList = formKeys.Join(";");
      }
      sqlResult = _oppRepository.startTestOpportunity (oppInstance, testKey, formKeyList);
      // Check if the opportunity has been started successfully.
      // NOTE: Make sure to check ReturnStatus because TestConfig is null when
      // not
      // started.
      if (sqlResult != null && sqlResult.getReturnStatus () != null && !"started".equalsIgnoreCase (sqlResult.getReturnStatus ().getStatus ())) {
        throw new ReturnStatusException (sqlResult.getReturnStatus ());
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
    return sqlResult;
  }

  // / <summary>
  // / Get all the segments for a test that has been started.
  // / </summary>
  public OpportunitySegments getSegments (OpportunityInstance oppInstance, boolean validate) throws ReturnStatusException {
    validate = true;
    try {
      OpportunitySegments sqlSegmentsResult = validate ? _oppRepository.getOpportunitySegments (oppInstance) : _oppRepository.getOpportunitySegments (oppInstance.getKey ());

      if (sqlSegmentsResult.getReturnStatus () != null) {
        throw new ReturnStatusException (sqlSegmentsResult.getReturnStatus ());
      }

      OpportunitySegments oppSegments = sqlSegmentsResult;
      return oppSegments;
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  // / <summary>
  // / Request approval to enter into a segment.
  // / </summary>
  public void waitForSegment (OpportunityInstance oppInstance, int segmentPosition, TestSegmentApproval segmentApproval) throws ReturnStatusException {
    ReturnStatus waitStatus;
    try {
      if (segmentApproval == TestSegmentApproval.Entry) {
        waitStatus = _oppRepository.waitForSegment (oppInstance, segmentPosition, true, false);
        if (!waitStatus.getStatus ().equalsIgnoreCase ("segmentEntry")) {
          throw new ReturnStatusException (waitStatus);
        }
      } else {
        waitStatus = _oppRepository.waitForSegment (oppInstance, segmentPosition, false, true);
        if (!waitStatus.getStatus ().equalsIgnoreCase ("segmentExit")) {
          throw new ReturnStatusException (waitStatus);
        }
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
  }

  public void exitSegment (OpportunityInstance oppInstance, int segmentPosition) throws ReturnStatusException {
    try {
      ReturnStatus returnStatus = _oppRepository.exitSegment (oppInstance, segmentPosition);
      // check if successful
      if (returnStatus.getStatus () != "success") {
        throw new ReturnStatusException (returnStatus);
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }
}
