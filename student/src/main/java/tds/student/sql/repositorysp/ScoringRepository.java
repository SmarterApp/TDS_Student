/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repositorysp;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.student.sql.abstractions.IScoringRepository;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.ItemDisplayScore;
import tds.student.sql.data.ItemDisplayScores;
import tds.student.sql.data.ItemResponseScorable;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.TestDisplayScore;
import tds.student.sql.data.TestDisplayScores;
import tds.student.sql.data.TestScoreInput;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class ScoringRepository extends AbstractDAO implements IScoringRepository
{
  private static final Logger _logger = LoggerFactory.getLogger (ScoringRepository.class);

  public ScoringRepository () {
    super ();
  }

  // / <summary>
  // / Update the score for an item.
  // / </summary>
  // / <remarks>
  // / This score is probably coming from item score server.
  // /
  // / From Larry: This will return a "failed" condition under possibly "normal"
  // circumstances
  // / in which a "demon" has grabbed the response for scoring thus altering the
  // mark.
  // / </remarks>
  public ReturnStatus updateItemScore (UUID oppKey, IItemResponseScorable responseScorable, int score, String scoreStatus, String scoreRationale, String scoreDimensions) throws ReturnStatusException {
    final String CMD_GET_UPDATE_ITEM_SCORE = "BEGIN; SET NOCOUNT ON; exec S_UpdateItemScore ${oppKey}, ${itemkey},${position}, ${sequence}, ${score}, ${scorestatus}, ${scoreRationale}, ${scoremark} ; end;";
    ReturnStatus returnStatus = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppKey", oppKey);
      parametersQuery.put ("itemkey", responseScorable.getItemKey ());
      parametersQuery.put ("position", responseScorable.getPosition ());
      parametersQuery.put ("sequence", responseScorable.getSequence ());
      parametersQuery.put ("score", score);
      parametersQuery.put ("scorestatus", scoreStatus);
      parametersQuery.put ("scoreRationale", scoreRationale);
      parametersQuery.put ("scoremark", responseScorable.getScoreMark ());

      SingleDataResultSet results = executeStatement (connection, CMD_GET_UPDATE_ITEM_SCORE, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();

      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        returnStatus = ReturnStatus.parse (record);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  // / <summary>
  // / Get the delimited item string used for passing to Paul's test scorer. We
  // can use
  // / this SP for when checking if the test is completed.
  // / </summary>
  public String getTestforCompleteness (UUID oppKey) throws ReturnStatusException {
    String scorestring = null;
    final String CMD_GET_UPDATE_ITEM_SCORE = "BEGIN; SET NOCOUNT ON; exec T_GetTestforCompleteness ${oppkey}, ${rowdelim}, ${coldelim}; end;";
    try (SQLConnection connection = getSQLConnection ()) {

      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppKey);
      parametersQuery.put ("rowdelim", ";");
      parametersQuery.put ("coldelim", ":");

      SingleDataResultSet results = executeStatement (connection, CMD_GET_UPDATE_ITEM_SCORE, parametersQuery, false).getResultSets ().next ();

      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();

      if (records.hasNext ()) {

        DbResultRecord record = records.next ();
        scorestring = record.<String> get ("scorestring");
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return scorestring;
  }

  // / <summary>
  // / Get the delimited item string used for passing to Paul's test scorer.
  // / </summary>
  // / <returns>
  // / Status: failed (has reason), Official score, Unofficial score
  // / String: Delimited string of the item responses
  // / </returns>
  public TestScoreInput getTestForScoring (UUID oppKey) throws ReturnStatusException {
    TestScoreInput testScoreInput = new TestScoreInput ();
    final String CMD_GET_UPDATE_ITEM_SCORE = "BEGIN; SET NOCOUNT ON; exec T_GetTestForScoring ${oppkey}, ${rowdelim}, ${coldelim}; end;";
    try (SQLConnection connection = getSQLConnection ()) {

      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppKey);
      parametersQuery.put ("rowdelim", ";");
      parametersQuery.put ("coldelim", ":");

      SingleDataResultSet results = executeStatement (connection, CMD_GET_UPDATE_ITEM_SCORE, parametersQuery, false).getResultSets ().next ();
      try {
        ReturnStatusException.getInstanceIfAvailable (results, "T_GetTestForScoring return no records");
      } catch (ReturnStatusException re) {
        testScoreInput.setReturnStatus (re.getReturnStatus ());
      }
      Iterator<DbResultRecord> records = results.getRecords ();
      DbResultRecord record = records.next ();
      // if we are using in services the ReturnStatus we will revisit this
      // issue.
      if (record.hasColumn ("itemstring")) {
        testScoreInput.setItemString (record.<String> get ("itemstring"));
        testScoreInput.setDateCompleted (record.<Date> get ("dateCompleted"));
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testScoreInput;
  }

  // / <summary>
  // / Check a score string to see if we can complete the test.
  // / </summary>
  // / <returns>
  // / a. 0 … completeness not valid
  // / b. 1 … completeness valid
  // / c. -1 … undetermined (this means that the required row in the scorestring
  // was not found)
  // / </returns>
  public int validateCompleteness (UUID oppKey, String scoreString) throws ReturnStatusException {
    int isValid = -1;
    final String CMD_GET_VALIDATE_COMPLETENESS = "BEGIN; SET NOCOUNT ON; exec T_ValidateCompleteness ${oppkey}, ${scoreString}, ${rowdelim}, ${coldelim}; end;";
    try (SQLConnection connection = getSQLConnection ()) {

      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppKey);
      parametersQuery.put ("scoreString", scoreString);
      parametersQuery.put ("rowdelim", ";");
      parametersQuery.put ("coldelim", ":");

      SingleDataResultSet results = executeStatement (connection, CMD_GET_VALIDATE_COMPLETENESS, parametersQuery, false).getResultSets ().next ();

      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();

      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        isValid = record.<Integer> get ("isValid");
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return isValid;
  }

  // / <summary>
  // / Insert the delimited score string that came from Paul's test scorer.
  // / </summary>
  // / <returns>
  // / If true is returned then this is an offical score.
  // / </returns>
  public ReturnStatus insertTestScores (UUID oppKey, String scoreString) throws ReturnStatusException {
    ReturnStatus returnstatus = null;
    final String CMD_GET_VALIDATE_COMPLETENESS = "BEGIN; SET NOCOUNT ON; exec S_InsertTestScores ${oppkey}, ${scoreString}, ${rowdelim}, ${coldelim} ; end;";
    try (SQLConnection connection = getSQLConnection ()) {

      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppKey);
      parametersQuery.put ("scoreString", scoreString);
      parametersQuery.put ("rowdelim", ";");
      parametersQuery.put ("coldelim", ",");

      SingleDataResultSet results = executeStatement (connection, CMD_GET_VALIDATE_COMPLETENESS, parametersQuery, false).getResultSets ().next ();

      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();
      returnstatus = ReturnStatus.readAndParse (results);
      // TODO
      // while (records.hasNext ()) {
      // DbResultRecord record = records.next ();
      // returnstatus = ReturnStatus.readAndParse (record);
      // }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return returnstatus;
  }

  // / <summary>
  // / Submit test to QA
  // / </summary>
  // / <returns>This should return a status of "success".</returns>
  public ReturnStatus submitQAReport (UUID oppKey) throws ReturnStatusException {
    ReturnStatus returnstatus = null;
    final String CMD_GET_SUBMIT_QA_REPORT = "BEGIN; SET NOCOUNT ON; exec SubmitQAReport ${oppkey}; end;";
    try (SQLConnection connection = getSQLConnection ()) {

      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppKey);

      SingleDataResultSet results = executeStatement (connection, CMD_GET_SUBMIT_QA_REPORT, parametersQuery, false).getResultSets ().next ();

      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();
      returnstatus = ReturnStatus.readAndParse (results);
      // while (records.hasNext ()) {
      // DbResultRecord record = records.next ();
      // returnstatus = ReturnStatus.readAndParse (record);
      // }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return returnstatus;
  }

  // / <summary>
  // / Get a students test scores for displaying at the end of the test.
  // / </summary>
  public TestDisplayScores getDisplayScores (UUID oppKey) throws ReturnStatusException {
    TestDisplayScores testScores = new TestDisplayScores ();
    final String CMD_GET_SUBMIT_QA_REPORT = "BEGIN; SET NOCOUNT ON; exec T_GetDisplayScores ${oppkey}; end;";

    try (SQLConnection connection = getSQLConnection ()) {
      // build parameters
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppKey);

      MultiDataResultSet multiDataResultSet = executeStatement (connection, CMD_GET_SUBMIT_QA_REPORT, parametersQuery, false);
      Iterator<SingleDataResultSet> results = multiDataResultSet.getResultSets ();

      if (results.hasNext ()) {
        SingleDataResultSet firstResultSet = results.next ();
        ReturnStatusException.getInstanceIfAvailable (firstResultSet);
        Iterator<DbResultRecord> records = firstResultSet.getRecords ();
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          if (record.<String> get ("ReportLabel") == null)
            continue;
          TestDisplayScore testScore = new TestDisplayScore ();
          testScore.setLabel (record.<String> get ("ReportLabel"));
          testScore.setvalue (record.<Object> get ("value").toString ()); // NOTE:
                                                                          // The
                                                                          // response
                                                                          // might
                                                                          // not
                                                                          // be
                                                                          // a
                                                                          // string
          testScores.add (testScore);
        }
        if (results.hasNext ()) {
          SingleDataResultSet secondResultSet = results.next ();
          Iterator<DbResultRecord> secondrecords = secondResultSet.getRecords ();
          if (secondrecords.hasNext ()) {
            DbResultRecord record = secondrecords.next ();
            testScores.setScoreByTDS (record.<Boolean> get ("scoreByTDS"));
            testScores.setShowScores (record.<Boolean> get ("showscores"));

            // set score status
            if (record.<String> get ("scorestatus") != null) {
              testScores.SetScoreStatus (record.<String> get ("scorestatus"));
            }
          }
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testScores;
  }

  // / <summary>
  // / Get a students item scores for displaying at the end of the test.
  // / </summary>
  public ItemDisplayScores getResponseRationales (OpportunityInstance oppInstance) throws ReturnStatusException {
    ItemDisplayScores itemScores = new ItemDisplayScores ();

    final String CMD_GET_VALIDATE_COMPLETENESS = "BEGIN; SET NOCOUNT ON; exec T_GetResponseRationales ${oppkey}, ${sessionKey}, ${browserKey}; end;";
    try (SQLConnection connection = getSQLConnection ()) {

      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("sessionKey", oppInstance.getSessionKey ());
      parametersQuery.put ("browserKey", oppInstance.getBrowserKey ());

      SingleDataResultSet results = executeStatement (connection, CMD_GET_VALIDATE_COMPLETENESS, parametersQuery, false).getResultSets ().next ();

      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();

        ItemDisplayScore itemScore = new ItemDisplayScore ();
        Integer Position = record.<Integer> get ("Position");
        if (Position != null)
          itemScore.setPosition (Position.intValue ());
        Integer page = record.<Integer> get ("Page");
        if (page != null)
          itemScore.setPage (page.intValue ());
        itemScore.setFormat (record.<String> get ("Format"));
        itemScore.setScore (record.<Integer> get ("Score"));
        Integer ScorePoint = record.<Integer> get ("ScorePoint");
        if (ScorePoint != null)
          itemScore.setScoreMax (ScorePoint.intValue ());

        // BUG #58546: Response can be null (but why?)
        // reader.FixNulls = true;
        itemScore.setResponse (record.<String> get ("Response"));
        itemScore.setScoreRationale (record.<String> get ("ScoreRationale"));

        itemScores.add (itemScore);

      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return itemScores;
  }

  // / <summary>
  // / This procedure is called by the item scoring engine to pick up a backlog
  // of items that require scoring
  // / It is needed to recover in case of a scoring engine failure
  // / This procedure attempts to guard against race conditions providing
  // duplicate items to different scoring engines by marking
  // / </summary>
  // / <param name="pendingMinutes"></param>
  // / <param name="minAttempts"> </param>
  // / <param name="maxAttempts"> </param>
  // / <param name="sessionType"> </param>
  public List<Map.Entry<UUID, IItemResponseScorable>> getScoreItems (int pendingMinutes, int minAttempts, int maxAttempts, int sessionType) throws ReturnStatusException {
    List<Map.Entry<UUID, IItemResponseScorable>> scoreItems = new ArrayList<Map.Entry<UUID, IItemResponseScorable>> ();
    final String CMD_GET_VALIDATE_COMPLETENESS = "BEGIN; SET NOCOUNT ON; exec S_GetScoreItems ${clientName}, ${pendingMinutes}, ${minAttempts}, ${maxAttempts}, ${sessiontype}; end;";
    try (SQLConnection connection = getSQLConnection ()) {

      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      
      parametersQuery.put ("clientname", getTdsSettings().getClientName ());
      parametersQuery.put ("pendingMinutes", pendingMinutes);
      parametersQuery.put ("minAttempts", minAttempts);
      parametersQuery.put ("maxAttempts", maxAttempts);
      parametersQuery.put ("sessiontype", sessionType);

      SingleDataResultSet results = executeStatement (connection, CMD_GET_VALIDATE_COMPLETENESS, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();

      while (records.hasNext ()) {
    	DbResultRecord record = records.next ();
    	try {
    	  UUID oppKey = record.<UUID> get ("oppKey");
    	  ItemResponseScorable responseScorable = new ItemResponseScorable (record.<String> get ("testkey"), record.<String> get ("testID"), record.<String> get ("Language"), record.<Integer> get ("position"),
    				  record.<Integer> get ("ResponseSequence"), record.<Long> get ("BankKey"), record.<Long> get ("ItemKey"), record.<String> get ("segmentID"), record.<String> get ("response"),
    				  record.<UUID> get ("scoremark"), record.<String> get ("ItemFile"));

    	  Map.Entry<UUID, IItemResponseScorable> entry = new AbstractMap.SimpleEntry<UUID, IItemResponseScorable> (oppKey, responseScorable);
    	  scoreItems.add (entry);
    	}
    	catch (Exception ex) {
    	  // NOTE: Balaji said it is ok not to log this right now but we need to "later"
    	}
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return scoreItems;
  }
}
