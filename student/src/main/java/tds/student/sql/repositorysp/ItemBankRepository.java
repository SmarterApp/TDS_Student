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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.student.sql.abstractions.IItemBankRepository;
import tds.student.sql.data.AccList;
import tds.student.sql.data.AccList.Data;
import tds.student.sql.data.TestForm;
import tds.student.sql.data.TestGrade;
import tds.student.sql.data.TestProperties;
import tds.student.sql.data.TestSegment;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

@Component
@Scope ("prototype")
public class ItemBankRepository extends AbstractDAO implements IItemBankRepository
{
  private static final Logger _logger = LoggerFactory.getLogger (ItemBankRepository.class);

  public ItemBankRepository () {
    super ();
  }

  // / <summary>
  // / Returns a unique list of all selectable test keys.
  // / </summary>
  // / <remarks>
  // / You can use this to load tests into the scoring engine.
  // / </remarks>
  public List<String> listTests () throws ReturnStatusException {
    List<String> testKeys = new ArrayList<String> ();
    final String CMD_GET_LIST_TESTS = "BEGIN; SET NOCOUNT ON; exec IB_ListTests ${clientName}, ${sessiontype}; end;";
    SqlParametersMaps parametersQuery = new SqlParametersMaps ();
    parametersQuery.put ("clientname", getTdsSettings().getClientName ());
    parametersQuery.put ("sessiontype", getTdsSettings().getSessionType ());
    
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet results = executeStatement (connection, CMD_GET_LIST_TESTS, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        boolean isSelectable = record.<Boolean> get ("IsSelectable");
        if (isSelectable) {
          String testKey = record.<String> get ("_Key");
          if (!testKeys.contains (testKey))
            testKeys.add (testKey);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testKeys;
  }

  public TestProperties getTestProperties (String testKey) throws ReturnStatusException {
    TestProperties testProps = new TestProperties ();
    final String CMD_GET_TEST_PROPERTIES = "BEGIN; SET NOCOUNT ON; exec IB_GetTestProperties ${testKey}; end;";

    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("testKey", testKey);

      MultiDataResultSet results = executeStatement (connection, CMD_GET_TEST_PROPERTIES, parametersQuery, false);
      Iterator<SingleDataResultSet> iterSingleDataResultSet = results.getResultSets ();
      SingleDataResultSet firstResultSet = iterSingleDataResultSet.next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        testProps = new TestProperties ();
        testProps.setTestKey (testKey);
        testProps.setTestID (record.<String> get ("TestID").trim ());
        testProps.setDisplayName (record.<String> get ("DisplayName").trim ());
        testProps.setIsSelectable (record.<Boolean> get ("IsSelectable"));
        testProps.setValidateCompleteness (record.<Boolean> get ("validateCompleteness"));
        // get optional properties:
        // reader.setFixNulls (true);
        if (record.<String> get ("AccommodationFamily") != null)
          testProps.setAccFamily (record.<String> get ("AccommodationFamily"));
        if (record.<Integer> get ("SortOrder") != null)
          testProps.setSortOrder (record.<Integer> get ("SortOrder"));
        if (record.<String> get ("GradeCode") != null)
          testProps.setGrade (record.<String> get ("GradeCode").trim ());
        if (record.<String> get ("Subject") != null)
          testProps.setSubject (record.<String> get ("Subject").trim ());
        // TODO need to change it
        // get ScoreByTDS (TODO: Can this still sometimes be an int?)
        Object scoreByTDS = record.<Object> get ("ScoreByTDS");
        if (scoreByTDS instanceof Integer)
          testProps.setScoreByTDS (record.<Integer> get ("ScoreByTDS") == 1);
        else if (scoreByTDS instanceof Boolean)
          testProps.setScoreByTDS (record.<Boolean> get ("ScoreByTDS"));
        // not used:
        testProps.setMaxOpportunities (record.<Integer> get ("MaxOpportunities"));
        testProps.setMinItems (record.<Integer> get ("MinItems"));
        testProps.setMaxItems (record.<Integer> get ("MaxItems"));
        testProps.setPrefetch (record.<Integer> get ("Prefetch"));
        // requirements
        testProps.getRequirements ().lookup (getTdsSettings().getClientName (), testProps);
      }

      SingleDataResultSet secondResultSet = iterSingleDataResultSet.next ();
      Iterator<DbResultRecord> secondrecords = secondResultSet.getRecords ();
      while (secondrecords.hasNext ()) {
        DbResultRecord record = secondrecords.next ();
        TestSegment testSegment = new TestSegment ();
        testSegment.setId (record.<String> get ("segmentID"));
        testSegment.setPosition (record.<Integer> get ("segmentPosition"));
        testSegment.setLabel (record.<String> get ("SegmentLabel"));
        testSegment.setIsPermeable (record.<Integer> get ("IsPermeable"));
        testSegment.setEntryApproval (record.<Integer> get ("entryApproval"));
        testSegment.setExitApproval (record.<Integer> get ("exitApproval"));
        testSegment.setItemReview (record.<Boolean> get ("itemReview"));
        testProps.getSegments ().add (testSegment);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testProps;
  }

  public AccList getTestAccommodations (String testKey) throws ReturnStatusException {
    AccList accList = new AccList ();
    final String CMD_GET_TEST_PROPERTIES = "BEGIN; SET NOCOUNT ON; exec IB_GetTestAccommodations ${testKey}; end;";

    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("testKey", testKey);

      MultiDataResultSet results = executeStatement (connection, CMD_GET_TEST_PROPERTIES, parametersQuery, false);

      Iterator<SingleDataResultSet> iterSingleDataResultSet = results.getResultSets ();
      SingleDataResultSet firstResultSet = iterSingleDataResultSet.next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        AccList.Data accData = AccList.parseData (record);
        // HACK: Skip loading non-functional accommodations
        if (!accData.isFunctional ())
          continue;
        accList.add (accData);
      }

      SingleDataResultSet secondResultSet = iterSingleDataResultSet.next ();
      records = secondResultSet.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        accList.getDependencies ().add (AccList.parseDependency (record));
      }
      Collections.sort (accList, new Comparator<AccList.Data> ()
      {
        @Override
        public int compare (Data acc1, Data acc2) {
          if (acc1.getSegmentPosition () != acc2.getSegmentPosition ())
            return Integer.compare (acc1.getSegmentPosition (), acc2.getSegmentPosition ());
          if (acc1.getToolTypeSortOrder () != acc2.getToolTypeSortOrder ())
            return Integer.compare (acc1.getToolTypeSortOrder (), acc2.getToolTypeSortOrder ());
          if (acc1.getToolValueSortOrder () != acc2.getToolValueSortOrder ())
            return Integer.compare (acc1.getToolValueSortOrder (), acc2.getToolValueSortOrder ());
          return 0;
        }
      });

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return accList;
  }

  public List<String> getGrades () throws ReturnStatusException {
    final String CMD_GET_TEST_GRADES = "BEGIN; SET NOCOUNT ON; exec IB_GetTestGrades ${clientName}, ${testKey}, ${sessiontype}; end;";

    List<TestGrade> testGrades = new ArrayList<TestGrade> ();

    try (SQLConnection connection = getSQLConnection ()) {
      // build parameters
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      
      parametersQuery.put ("clientname", getTdsSettings().getClientName ());
      parametersQuery.put ("testKey", null);
      parametersQuery.put ("sessiontype", getTdsSettings().getSessionType ());
      
      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_TEST_GRADES, parametersQuery, false).getResultSets ().next ();

      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        TestGrade testGrade = new TestGrade (record.<String> get ("grade"));
        if (testGrades.contains (testGrade))
          continue;
        testGrades.add (testGrade);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    Collections.<TestGrade> sort (testGrades, new Comparator<TestGrade> ()
    {
      @Override
      public int compare (TestGrade o1, TestGrade o2) {
        int compare = Integer.compare (o1.getInteger (), o2.getInteger ());
        if (compare == 0)
          return o1.getText ().compareTo (o2.getText ());
        return compare;
      }
    });
    return (List<String>) ((CollectionUtils.collect (testGrades, new Transformer ()
    {
      public Object transform (Object each) {
        return ((TestGrade) each).getText ();
      }
    }, new ArrayList<String> ())));
  }

  public List<TestForm> getTestForms (String testID) throws ReturnStatusException {
    final String CMD_GET_TEST_FORMS = "BEGIN; SET NOCOUNT ON; exec P_GetTestForms ${clientName}, ${testID} , ${sessiontype}; end;";
    List<TestForm> testforms = new ArrayList<TestForm> ();
    try (SQLConnection connection = getSQLConnection ()) {
      // build parameters
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("clientname", getTdsSettings().getClientName ());
      parametersQuery.put ("testID", testID);
      parametersQuery.put ("sessiontype", getTdsSettings().getSessionType ());
      
      SingleDataResultSet results = executeStatement (connection, CMD_GET_TEST_FORMS, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        TestForm testForm = new TestForm ();
        testForm.setKey (record.<String> get ("formKey"));
        testForm.setId (record.<String> get ("formID"));
        testforms.add (testForm);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    Collections.sort (testforms, new Comparator<TestForm> ()
    {
      @Override
      public int compare (TestForm o1, TestForm o2) {
        return o1.getId ().compareTo (o2.getId ());
      }
    });

    return testforms;
  }

  public String getItemPath (long bankKey, long itemKey) throws ReturnStatusException {
    String itemPath = null;
    final String CMD_GET_ITEM_PATH = "BEGIN; SET NOCOUNT ON; exec IB_GetItemPath ${clientName}, ${bankKey}, ${itemKey}; end;";
    try (SQLConnection connection = getSQLConnection ()) {
      // build parameters
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      
      parametersQuery.put ("clientname", getTdsSettings().getClientName ());
      parametersQuery.put ("bankKey", bankKey);
      parametersQuery.put ("itemKey", itemKey);

      SingleDataResultSet results = executeStatement (connection, CMD_GET_ITEM_PATH, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        if (record.<String> get ("itempath") != null)
          itemPath = record.<String> get ("itempath");
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return itemPath;
  }

  public String getStimulusPath (long bankKey, long stimulusKey) throws ReturnStatusException {
    String stimulusPath = null;
    final String CMD_GET_ITEM_PATH = "BEGIN; SET NOCOUNT ON; exec IB_GetStimulusPath ${clientName}, ${bankKey}, ${stimulusKey}; end;";
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      
      parametersQuery.put ("clientname", getTdsSettings().getClientName ());
      parametersQuery.put ("bankKey", bankKey);
      parametersQuery.put ("stimulusKey", stimulusKey);

      SingleDataResultSet results = executeStatement (connection, CMD_GET_ITEM_PATH, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        if (record.<String> get ("stimuluspath") != null)
          stimulusPath = record.<String> get ("stimuluspath");
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return stimulusPath;
  }

}
