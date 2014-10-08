/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import scoringengine.Scorer;
import tds.student.services.abstractions.ITestScoringService;
import tds.student.services.data.TestScoreStatus;
import tds.student.sql.abstractions.IScoringRepository;
import tds.student.sql.data.TestScoreInput;
import tds.student.sql.singletons.ClientManager;
import tds.student.sql.singletons.ClientSingleton;
import AIR.Common.TDSLogger.ITDSLogger;
import TDS.Shared.Configuration.ITDSSettingsSource;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */

@Component
@Scope ("prototype")
public class TestScoringService implements ITestScoringService
{
  private ClientSingleton     _singleton;

  @Autowired
  private IScoringRepository  _scoringRepository;

  @Autowired
  private ITDSLogger          _tdsLogger;

  @Autowired
  private ITDSSettingsSource  _tdsSettings;

  private static final Logger _logger = LoggerFactory.getLogger (TestScoringService.class);

  @Autowired
  public TestScoringService (ITDSSettingsSource tdsSettings, ClientManager clientManager) {
    this._tdsSettings = tdsSettings;
    try {
      _singleton = clientManager.getClient (_tdsSettings.getClientName ());
    } catch (Exception e) {
      e.printStackTrace ();
    }
  }

  // / <summary>
  // / A wrapper function for Paul's test scoring engine.
  // / </summary>
  protected String getTestScoreString (String testKey, String input, Date testEndDate) {
    String scoreString = null;
    // make sure test for scoring is loaded
    // TODO mpatel - Discuss with Shiva and Create Scorer class

    _singleton.loadTestForScoring (testKey);
    // call test scorer engine with delimited response from DB
    Scorer testScorer = _singleton.CreateTestScorer ();

    try { // call Paul's test scoring engine scoreString =
      scoreString = testScorer.testScore (testKey, input, testEndDate, ';', ':', "Completed");
    } catch (Exception ex) {
      // Most likely a ScoringEngineException occured if we get here..
      if (_tdsSettings.isTestScoringLogError ()) {
        _tdsLogger.applicationError (ex.getMessage (), "getTestScoreString", null, ex);
      }
      return null;
    }
    return scoreString;
  }

  // / <summary>
  // / Check if we can complete the test and perform scoring.
  // / </summary>
  public boolean canCompleteTest (UUID oppKey, String testKey) throws ReturnStatusException {
    try {
      // get the delimited string used for test scorer engine
      String scoreStringInput = _scoringRepository.getTestforCompleteness (oppKey);

      // if empty then we don't have enough information to say this can be
      // completed
      if (StringUtils.isEmpty (scoreStringInput))
        return false;

      // try and score test
      Date date = new Date ();
      String scoreString = getTestScoreString (testKey, scoreStringInput, date);
      if (StringUtils.isEmpty (scoreString))
        return false;

      // check if completeness is valid
      int isValid = _scoringRepository.validateCompleteness (oppKey, scoreString);
      return (isValid == 1);
    } catch (ReturnStatusException e) {
      _logger.error (String.format ("TestScoringService.canCompleteTest error: ", e.getMessage ()));
      throw new ReturnStatusException (e);
    }
  }

  // / <summary>
  // / Scores the current test opportunity.
  // / </summary>
  // / <remarks>
  // / If this returns true the test was scored (or can't be scored by TDS and
  // we can move on).
  // / If this returns false then the test was not ready to be scored yet.
  // / </remarks>
  public TestScoreStatus scoreTest (UUID oppKey, String testKey) throws ReturnStatusException {
    try {
      // get the delimited string used for test scorer engine
      TestScoreInput sqlTestForScoringResult = _scoringRepository.getTestForScoring (oppKey);
      // Check 1: Is the opp ready for scoring in TDS.
      boolean oppReadyForScoringinTDS = sqlTestForScoringResult.getReturnStatus ().getStatus () != "failed";
      // Check 2: Is the opp ready to be submitted to QA
      boolean oppReadyforSubmissionToQA = oppReadyForScoringinTDS
          || ("failed".equalsIgnoreCase (sqlTestForScoringResult.getReturnStatus ().getStatus ()) && "COMPLETE: Do Not Score"
              .equalsIgnoreCase (sqlTestForScoringResult.getReturnStatus ().getReason ()));
      String message = String.format ("TestScoring: Get test for scoring - %s \" %s\"", sqlTestForScoringResult.getReturnStatus ().getStatus (), sqlTestForScoringResult.getReturnStatus ()
          .getReason ());

      boolean logDebug = _tdsSettings.isTestScoringLogDebug ();
      if (logDebug)
        _tdsLogger.applicationInfo (message, "scoreTest", null);
      // check if "failed" and the reason is something other than
      // "complete but dont score", which means the test is still being scored
      // (async) (don't submit to QA)
      // NOTE: You can find reasons listed in this SQL function:
      // CanScoreOpportunity
      if (!oppReadyforSubmissionToQA)
        return TestScoreStatus.Waiting;
      if (oppReadyForScoringinTDS) {
        TestScoreInput testScoreInput = sqlTestForScoringResult;
        // Check if empty responses to be scored (NOTE: this should never
        // happen)
        if (StringUtils.isEmpty (testScoreInput.getItemString ())) {
          if (_tdsSettings.isTestScoringLogError ())
            _tdsLogger.applicationError ("TestScoring: Cannot score this test because T_GetTestForScoring returned an empty item string.", "scoreTest", null, null);
          return TestScoreStatus.Error;
        }
        // try and score test
        String scoreString = getTestScoreString (testKey, testScoreInput.getItemString (), testScoreInput.getDateCompleted ());
        if (StringUtils.isEmpty (scoreString)) {
          // We don't return error here. But if this happens the SP
          // S_InsertTestScores
          // won't get called and the test won't get submitted to QA.
          if (_tdsSettings.isTestScoringLogError ())
            _tdsLogger.applicationError ("TestScoring: Test Scoring Engine returned an empty score string.", "scoreTest", null, null);
        } else {
          // Call S_InsertTestScores to save delimited scores to DB
          // NOTE: This also sets status to 'Scored' and submits test to QA
          try {
            // java implementation throws exception or returns null in case of success
            _scoringRepository.insertTestScores (oppKey, scoreString);
          } catch (ReturnStatusException rr) {

            if (_tdsSettings.isTestScoringLogError ()) {
              String msg = String.format ("TestScoring: S_InsertTestScores has invalid return status of '%s': %s",
                  rr.getReturnStatus ().getStatus (),
                  (rr.getReturnStatus ().getReason () == null ? "" : rr.getReturnStatus ().getReason ()));

              _tdsLogger.applicationError (msg, "scoreTest", null, null);
            }
            return TestScoreStatus.Error;
          }
        }
      }
      // Tell server to submit to QA (Larry said to ignore errors from this)
      // NOTE: This does not seem to actually submit test to QA
      try {
        ReturnStatus submitQaReportStatus = _scoringRepository.submitQAReport (oppKey);
      } catch (Exception ex) {
        // ignore as per comment above
        _logger.warn ("TestScoringService.scoreTest submitQAReport  error: " + ex.getMessage ());
      }
      return TestScoreStatus.Submitted;
    } catch (ReturnStatusException e) {
      _logger.error (String.format ("TestScoringService.scoreTest error: ", e.getMessage ()));
      throw new ReturnStatusException (e);
    }
  }
}
