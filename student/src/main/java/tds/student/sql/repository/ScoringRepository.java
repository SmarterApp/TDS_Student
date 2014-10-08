/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repository;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IStudentDLL;
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
  private static final Logger _logger     = LoggerFactory.getLogger (SessionRepository.class);
  @Autowired
  private ICommonDLL          _commonDll  = null;
  @Autowired
  private IStudentDLL         _studentDll = null;

  public ScoringRepository () {
    super ();
  }

  public void setiCommonDLL (ICommonDLL _dll) {
    _commonDll = _dll;
  }

  public void setiStudentDLL (IStudentDLL _dll) {
    _studentDll = _dll;
  }

  public ReturnStatus updateItemScore (UUID oppKey, IItemResponseScorable responseScorable, int score, String scoreStatus, String scoreRationale, String scoreDimensions) throws ReturnStatusException {
    ReturnStatus returnStatus = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.S_UpdateItemScore_SP (connection, oppKey, responseScorable.getItemKey (), responseScorable.getPosition (), responseScorable.getSequence (),
          score, scoreStatus, (scoreRationale != null ? scoreRationale : ""), responseScorable.getScoreMark (), scoreDimensions);
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

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

  public String getTestforCompleteness (UUID oppKey) throws ReturnStatusException {
    String scorestring = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_GetTestforCompleteness_SP (connection, oppKey, ';', ':');
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

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

  public TestScoreInput getTestForScoring (UUID oppKey) throws ReturnStatusException {
    TestScoreInput testScoreInput = new TestScoreInput ();
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_GetTestforScoring_SP (connection, oppKey, ';', ':');
      // ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      DbResultRecord record = records.next ();
      // if we are using in services the ReturnStatus we will revisit this
      // issue.
      if (record.hasColumn ("itemstring")) {
        testScoreInput.setItemString (record.<String> get ("itemstring"));
        testScoreInput.setDateCompleted (record.<Date> get ("dateCompleted"));
      }
      testScoreInput.setReturnStatus (ReturnStatus.readAndParse (firstResultSet));
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testScoreInput;
  }

  public int validateCompleteness (UUID oppKey, String scoreString) throws ReturnStatusException {
    int isValid = -1;
    try (SQLConnection connection = getSQLConnection ()) {
      isValid = _studentDll.T_ValidateCompleteness_SP (connection, oppKey, scoreString, ';', ':');
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return isValid;
  }

  public ReturnStatus insertTestScores (UUID oppKey, String scoreString) throws ReturnStatusException {
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.S_InsertTestScores_SP (connection, oppKey, scoreString, ';', ':');
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return null;
  }

  public ReturnStatus submitQAReport (UUID oppKey) throws ReturnStatusException {
    SingleDataResultSet res = null;
    ReturnStatus returnStatus = null;
    try (SQLConnection connection = getSQLConnection ()) {
      res = _commonDll.SubmitQAReport_SP (connection, oppKey, "submitted");
      if (res != null)
        ReturnStatusException.getInstanceIfAvailable (res);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    if (res != null) {
      Iterator<DbResultRecord> records = res.getRecords ();

      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        returnStatus = ReturnStatus.parse (record);
      }
    }
    return returnStatus;
  }

  public TestDisplayScores getDisplayScores (UUID oppKey) throws ReturnStatusException {
    TestDisplayScores testScores = new TestDisplayScores ();

    try (SQLConnection connection = getSQLConnection ()) {
      MultiDataResultSet resultSets = _studentDll.T_GetDisplayScores_SP (connection, oppKey);
      Iterator<SingleDataResultSet> results = resultSets.getResultSets ();
      // first expected result set
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
          testScore.setvalue (record.<Object> get ("value").toString ());
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

  public ItemDisplayScores getResponseRationales (OpportunityInstance oppInstance) throws ReturnStatusException {
    ItemDisplayScores itemScores = new ItemDisplayScores ();

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_GetResponseRationales_SP (connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey ());
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

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

  public List<Map.Entry<UUID, IItemResponseScorable>> getScoreItems (int pendingMinutes, int minAttempts, int maxAttempts, int sessionType) throws ReturnStatusException {
    List<Map.Entry<UUID, IItemResponseScorable>> scoreItems = new ArrayList<Map.Entry<UUID, IItemResponseScorable>> ();
    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet firstResultSet = _studentDll.S_GetScoreItems_SP (connection, getTdsSettings ().getClientName (), pendingMinutes, minAttempts, maxAttempts, sessionType);
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

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
