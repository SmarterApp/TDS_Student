/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repositorysp;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.ItemDisplayScore;
import tds.student.sql.data.ItemDisplayScores;
import tds.student.sql.data.ItemResponseScorable;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.TestDisplayScores;
import tds.student.sql.data.TestScoreInput;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;


/**
 * @author temp_rreddy
 * 
 */
public class ScoringRepositoryTest extends AbstractTest
{

  private static final Logger _logger = LoggerFactory.getLogger(ScoringRepositoryTest.class);

  ScoringRepository scoringRepository = new ScoringRepository ();

  @Test
  @Ignore("fix missing scoreDimensions parameter in the call to scoringRepository.updateItemScore")
  public void testUpdateItemScore () throws SQLException, ReturnStatusException {
    try {

      UUID oppKey = UUID.fromString ("42863E38-DD83-45CC-A72B-4EF2B8B32E69");
      Long itemKey = 100639L;
      Integer position = 4;
//      Integer sequence = 1;
      Integer score = -999;
      String scorestatus = "test for S_UpdateItemScore - scorestatus";
      String scoreRationale = "test for S_UpdateItemScore - scoreRationale";
      ItemResponseScorable itemResponseScorable = new ItemResponseScorable ();
      itemResponseScorable.setItemKey (itemKey);
      itemResponseScorable.setPosition (position);
      UUID scoremark = UUID.fromString ("ED35FE96-82D1-49B3-9BAB-34E535B59A5C");
      itemResponseScorable.setScoreMark (scoremark);
     //TODO: scoreDimensions set temporarily to null. Put the right scoreDimensions
      ReturnStatus returnStatus = scoringRepository.updateItemScore
          (oppKey, itemResponseScorable, score, scorestatus, scoreRationale, null);
      _logger.info ("Return Status Value::" + returnStatus.getStatus ());
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  @Test
  public void testTestforCompletenessFailue () throws SQLException, ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
      String testCompleteness = scoringRepository.getTestforCompleteness (oppKey);
      Assert.assertTrue (testCompleteness != null);
      if (testCompleteness != null)
        _logger.info ("Test Completeness Value::" + testCompleteness);
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  // Failure
  @Test
  public void testTestforCompleteness () throws SQLException, ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
      String testCompleteness = scoringRepository.getTestforCompleteness (oppKey);
      assertTrue(testCompleteness == null);
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  @Test
  public void testTestForScoring () throws SQLException, ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("C5FAD533-8F95-4ECF-AB79-02A8D2AB3C40");
      TestScoreInput testScoreInput = scoringRepository.getTestForScoring (oppKey);
      Assert.assertTrue (testScoreInput != null);
      if (testScoreInput != null) {
        _logger.info ("Test Completeness Value::" + testScoreInput.getItemString ());
        _logger.info ("Update Item Score Value::" + testScoreInput.getDateCompleted ());
        _logger.info ("Update Item Score Value::" + testScoreInput.getReturnStatus ());
      }
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  // Failure
  @Test
  public void testTestForScoringFailure () throws SQLException, ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("C5FAD533-8F95-4ECF-AB79-02A8D2AB3C30");
      TestScoreInput testScoreInput = scoringRepository.getTestForScoring (oppKey);
      assertTrue(testScoreInput == null);
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  @Test
  public void testValidateCompleteness () throws SQLException, ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("7483AC2F-F74F-4D6C-9EBD-1E4ADC89859E");
      String scoresString = "Overall:Attempted:99:x;asdasd:asasd:00;Overall:Attempted:0:x;Overall:Attempted:0:x;1231:12312";
      int validatecompletenessvalue = scoringRepository.validateCompleteness (oppKey, scoresString);
      Assert.assertTrue (validatecompletenessvalue != -1);
      if (validatecompletenessvalue != -1) {
        _logger.info ("validate completeness value::" + validatecompletenessvalue);
      }
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  // Failure
  @Test
  public void testValidateCompletenessFailue () throws SQLException, ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("7483AC2F-F74F-4D6C-9EBD-1E4ADd89859E");
      String scoresString = "Overall:Attempted:99:x;asdasd:asasd:00;Overall:Attempted:0:x;Overall:Attempted:0:x;1231:12312";
      int validatecompletenessvalue = scoringRepository.validateCompleteness (oppKey, scoresString);
      assertTrue(validatecompletenessvalue == 0);
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  // Success
  @Test
  public void testInsertTestScores () throws SQLException, ReturnStatusException {
    try {

      UUID oppKey = UUID.fromString ("8A1B6F1C-00BA-4171-AB8D-8AF50D994E29");
      String scoreString = "159-457,0,OP,12.345E-3;159-458,-1,OP;159-459,-1,OP;159-460,-1,OP;159-461,-1,OP;159-462,1,OP;159-475,0,OP;159-463,0,OP;159-464,1,OP;159-465,-1,OP;159-466,0,OP;159-467,0,OP;159-468,1,OP;159-469,0,OP;159-470,-1,OP;159-526,1,OP;159-518,0,OP;159-519,-1,OP;159-520,1,OP;159-515,,OP;159-523,,OP;159-522,,OP;159-521,,OP;159-524,,OP";

      ReturnStatus returnStatus = scoringRepository.insertTestScores (oppKey, scoreString);
      Assert.assertTrue (returnStatus != null);
      if (returnStatus != null)
        _logger.info ("Return Status Value::" + returnStatus.getStatus ());
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      assertTrue(returnStatus.getReason ().equalsIgnoreCase ("The session keys do not match; please consult your test administrator [-----] "));
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  // //Failure
  @Test
  public void testInsertTestScoresFailure () throws SQLException, ReturnStatusException {
    try {

      UUID oppKey = UUID.fromString ("8A1B6F1C-00BA-4171-AB8D-8AF50D994E21");
      String scoreString = "159-457,0,OP,12.345E-3;159-458,-1,OP;159-459,-1,OP;159-460,-1,OP;159-461,-1,OP;159-462,1,OP;159-475,0,OP;159-463,0,OP;159-464,1,OP;159-465,-1,OP;159-466,0,OP;159-467,0,OP;159-468,1,OP;159-469,0,OP;159-470,-1,OP;159-526,1,OP;159-518,0,OP;159-519,-1,OP;159-520,1,OP;159-515,,OP;159-523,,OP;159-522,,OP;159-521,,OP;159-524,,OP";

      ReturnStatus returnStatus = scoringRepository.insertTestScores (oppKey, scoreString);
      Assert.assertTrue (returnStatus != null);
      if (returnStatus != null)
        _logger.info ("Return Status Value::" + returnStatus.getStatus ());
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      assertTrue(returnStatus.getReason ().equalsIgnoreCase ("The session keys do not match; please consult your test administrator [-----] "));
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  // Success
  @Test
  public void testSubmitQAReport () throws SQLException, ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("C70B55D2-2A37-4E96-922B-00CF8CA43638");
      ReturnStatus returnStatus = scoringRepository.submitQAReport (oppKey);
      Assert.assertTrue (returnStatus != null);
      if (returnStatus != null)
        _logger.info ("Return Status Value::" + returnStatus.getStatus ());
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  // //Failure
  @Test
  public void testSubmitQAReportFailure () throws SQLException, ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("C70B55D2-2A37-4E96-922B-00CF8CA43628");
      ReturnStatus returnStatus = scoringRepository.submitQAReport (oppKey);
      Assert.assertTrue (returnStatus != null);
      if (returnStatus != null) {
        _logger.info ("Return Status Value::" + returnStatus.getStatus ());
        _logger.info ("Return Reason Value::" + returnStatus.getReason ());
      }
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  //
  @Test
  public void testGetDisplayScores () throws SQLException, ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("8B310C5E-D4C8-4CD6-96C3-394B2F14DA88");
      TestDisplayScores testDisplayScores = scoringRepository.getDisplayScores (oppKey);
      Assert.assertTrue (testDisplayScores != null);

      if (testDisplayScores != null)
      {
        _logger.info ("test Display Scores completed value ::" + testDisplayScores.isCompleted ());
        _logger.info ("test Display ScoreByTDS Value::" + testDisplayScores.isScoreByTDS ());
        _logger.info ("test Display Scoreed Value::" + testDisplayScores.isScored ());
        _logger.info ("test Display ShowScores Value::" + testDisplayScores.isShowScores ());
      }
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      assertTrue(returnStatus.getReason ().equalsIgnoreCase ("The session keys do not match; please consult your test administrator [-----] "));
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  // Failure
  @Test
  public void testGetDisplayScoresFailue () throws SQLException, ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("8B310C5E-D4C8-4CD6-96C3-394B2F24DA88");
      TestDisplayScores testDisplayScores = scoringRepository.getDisplayScores (oppKey);
      assertTrue(testDisplayScores == null);
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      assertTrue(returnStatus.getReason ().equalsIgnoreCase ("The session keys do not match; please consult your test administrator [-----] "));
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  @Test
  public void testGetResponseRationales () throws SQLException, ReturnStatusException {
    try {

      UUID oppKey = UUID.fromString ("EDEA2279-BF19-406F-A41D-74BF47814B");
      UUID sessionkey = UUID.fromString ("0A94BDC9-86E7-43B7-82FD-4CDB0AF08EC2");
      UUID browserkey = UUID.fromString ("6D301A09-A325-4D06-AC0E-10DB5C7DF0E3");

      OpportunityInstance OpportunityInstance = new
          OpportunityInstance (oppKey, sessionkey, browserkey);
      ItemDisplayScores itemDisplayScores =
          scoringRepository.getResponseRationales (OpportunityInstance);
      Assert.assertTrue (itemDisplayScores != null);
      if (itemDisplayScores != null) {

        for (int i = 0; i < itemDisplayScores.size (); i++) {
          ItemDisplayScore ItemDisplayScore = itemDisplayScores.get (i);
          _logger.info ("Item Display Score format value:" + ItemDisplayScore.getFormat ());
          _logger.info ("Item Display Score format value:" + ItemDisplayScore.getPage ());
          _logger.info ("Item Display Score format value:" + ItemDisplayScore.getPosition ());
          _logger.info ("Item Display Score format value:" + ItemDisplayScore.getResponse ());
          _logger.info ("Item Display Score format value:" + ItemDisplayScore.getScore ());
          _logger.info ("Item Display Score format value:" + ItemDisplayScore.getScoreMax ());
          _logger.info ("Item Display Score format value:" + ItemDisplayScore.getScoreRationale ());
        }

      }
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      assertTrue(returnStatus.getReason ().equalsIgnoreCase ("The session keys do not match; please consult your test administrator [-----] "));
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  // Failure
  @Test
  public void testGetResponseRationalesFailue () throws SQLException, ReturnStatusException {
    try {

      UUID oppKey = UUID.fromString ("EDEA2279-BF19-406F-A41D-74BF47814B");
      UUID sessionkey = UUID.fromString ("0A94BDC9-86E7-43B7-82FD-4CDB0AF08EC2");
      UUID browserkey = UUID.fromString ("6D301A09-A325-4D06-AC0E-10DB5C7DF0E3");

      OpportunityInstance OpportunityInstance = new
          OpportunityInstance (oppKey, sessionkey, browserkey);
      ItemDisplayScores itemDisplayScores =
          scoringRepository.getResponseRationales (OpportunityInstance);
      assertTrue(itemDisplayScores == null);
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      assertTrue(returnStatus.getReason ().equalsIgnoreCase ("The session keys do not match; please consult your test administrator [-----] "));
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  // Success
  @Test
  public void testGetScoreItems () throws SQLException, ReturnStatusException {
    try {
      int pendingMinutes = 0;
      int minAttempts = 0;
      int maxAttempts = 20000;
      int sessionType = 0;

      List<Map.Entry<UUID, IItemResponseScorable>> scoreitems = scoringRepository.getScoreItems (pendingMinutes, minAttempts, maxAttempts, sessionType);
      Assert.assertTrue (scoreitems != null);
      if (scoreitems != null) {
        _logger.info ("Opportunity Instance Value::" + scoreitems.size ());

        for (int i = 0; i < scoreitems.size (); i++) {
          Map.Entry<UUID, IItemResponseScorable> ItemDisplayScore = scoreitems.get (i);
          _logger.info ("Item Display Score format value" + ItemDisplayScore.getKey ());
          IItemResponseScorable iItemResponseScorable = ItemDisplayScore.getValue ();
          _logger.info ("IItemResponseScorable Bank Key" + iItemResponseScorable.getBankKey ());
          _logger.info ("IItemResponseScorable File Path" + iItemResponseScorable.getFilePath ());
          _logger.info ("IItemResponseScorable Item ID" + iItemResponseScorable.getItemID ());
          _logger.info ("IItemResponseScorable item key" + iItemResponseScorable.getItemKey ());
          _logger.info ("IItemResponseScorable language" + iItemResponseScorable.getLanguage ());
          _logger.info ("IItemResponseScorable position" + iItemResponseScorable.getPosition ());
          _logger.info ("IItemResponseScorable segment id" + iItemResponseScorable.getSegmentID ());
          _logger.info ("IItemResponseScorable sequence" + iItemResponseScorable.getSequence ());
          _logger.info ("IItemResponseScorable test key" + iItemResponseScorable.getTestKey ());
          _logger.info ("IItemResponseScorable value" + iItemResponseScorable.getValue ());
          _logger.info ("IItemResponseScorable score mark" + iItemResponseScorable.getScoreMark ());
        }
      }
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  // //Failure
  @Test
  public void testGetScoreItemsFailure () throws SQLException, ReturnStatusException {
    try {
      int pendingMinutes = 0;
      int minAttempts = 0;
      int maxAttempts = 0;
      int sessionType = 0;

      List<Map.Entry<UUID, IItemResponseScorable>> scoreitems = scoringRepository.getScoreItems (pendingMinutes, minAttempts, maxAttempts, sessionType);
      assertTrue(scoreitems == null);
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

}
