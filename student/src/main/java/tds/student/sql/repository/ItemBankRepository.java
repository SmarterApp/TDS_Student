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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IStudentDLL;
import tds.student.sql.abstractions.IItemBankRepository;
import tds.student.sql.data.AccList;
import tds.student.sql.data.AccListParseData;
import tds.student.sql.data.Data;
import tds.student.sql.data.TestForm;
import tds.student.sql.data.TestGrade;
import tds.student.sql.data.TestProperties;
import tds.student.sql.data.TestSegment;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * Moved to ItemBankService to utilize new optimized methods and use caching
 */
@Component
@Scope ("prototype")
@Deprecated
public class ItemBankRepository extends AbstractDAO implements IItemBankRepository
{
  private static Logger      _logger     = LoggerFactory.getLogger (ItemBankRepository.class);
  @Autowired
  private ICommonDLL  _commonDll  = null;
  @Autowired
  private IStudentDLL _studentDll = null;

  public ItemBankRepository () {
    super ();
  }

  public void setiCommonDLL (ICommonDLL _dll) {
    _commonDll = _dll;
  }

  public void setiStudentDLL (IStudentDLL _dll) {
    _studentDll = _dll;
  }

  public List<String> listTests () throws ReturnStatusException {

    List<String> testKeys = new ArrayList<String> ();

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _studentDll.IB_ListTests_SP (connection, getTdsSettings().getClientName (), getTdsSettings().getSessionType ());
      ReturnStatusException.getInstanceIfAvailable (result);

      Iterator<DbResultRecord> records = result.getRecords ();

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        boolean isSelectable = record.<Boolean> get ("IsSelectable");
        if (isSelectable) {
          String testKey = record.<String> get ("_Key");
          if (!testKeys.contains (testKey))
            testKeys.add (testKey);
        }
      }
    } catch (SQLException se) {
      throw new ReturnStatusException (se);
    }
    return testKeys;
  }

  /**
   * This has been replaced by ItemBankService and utilizes caching.
   *
   * @deprecated use {@link #ItemBankService.getTestProperties()} instead.
   */
  @Deprecated
  public TestProperties getTestProperties (String testKey) throws ReturnStatusException {
    TestProperties testProps = null;

    try (SQLConnection connection = getSQLConnection ()) {
      MultiDataResultSet resultSets = _studentDll.IB_GetTestProperties_SP (connection, testKey);

      Iterator<SingleDataResultSet> results = resultSets.getResultSets ();
      // first expected result set
      if (results.hasNext ()) {

        SingleDataResultSet firstResultSet = results.next ();
        ReturnStatusException.getInstanceIfAvailable (firstResultSet);

        // Elena: discussed with Shiva, we expect only one record in first data
        // set
        DbResultRecord record = firstResultSet.getRecords ().next ();
        if (record != null) {

          testProps = new TestProperties ();
          testProps.setTestKey (testKey);
          testProps.setTestID (record.<String> get ("TestID").trim ());
          testProps.setDisplayName (record.<String> get ("DisplayName").trim ());
          testProps.setIsSelectable (record.<Boolean> get ("IsSelectable"));
          testProps.setValidateCompleteness (record.<Boolean> get ("validateCompleteness"));
          // get optional properties:
          firstResultSet.setFixNulls (true);

          testProps.setAccFamily (record.<String> get ("AccommodationFamily"));

          testProps.setSortOrder (record.<Integer> get ("SortOrder"));

          testProps.setGrade (record.<String> get ("GradeCode").trim ());

          testProps.setSubject (record.<String> get ("Subject").trim ());

          testProps.setScoreByTDS (record.<Boolean> get ("ScoreByTDS"));
          // not used:
          testProps.setMaxOpportunities (record.<Integer> get ("MaxOpportunities"));
          testProps.setMinItems (record.<Integer> get ("MinItems"));
          testProps.setMaxItems (record.<Integer> get ("MaxItems"));
          testProps.setPrefetch (record.<Integer> get ("Prefetch"));
          // requirements
           testProps.getRequirements ().lookup (getTdsSettings().getClientName (), testProps);
        }

        if (testProps != null && results.hasNext ()) {
          SingleDataResultSet secondResultSet = results.next ();

          secondResultSet.setFixNulls (true);

          Iterator<DbResultRecord> records = secondResultSet.getRecords ();
          while (records.hasNext ()) {
            record = records.next ();
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
        }
      }

    } catch (SQLException se) {
      throw new ReturnStatusException (se);
    }
    return testProps;
  }

  /**
   * This has been replaced by ItemBankService.getTestAccommodations and utilizes caching.
   *
   * @deprecated use {@link #ItemBankService.getTestAccommodations()} instead.
   */
  @Deprecated
  public AccList getTestAccommodations (String testKey) throws ReturnStatusException {
    AccList accList = new AccList ();
    try (SQLConnection connection = getSQLConnection ()) {

      Iterator<SingleDataResultSet> results = _commonDll.IB_GetTestAccommodations_SP (connection, testKey).getResultSets ();
      if (results.hasNext ()) {
        SingleDataResultSet firstResultSet = results.next ();
        ReturnStatusException.getInstanceIfAvailable (firstResultSet);
        Iterator<DbResultRecord> records = firstResultSet.getRecords ();
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          Data accData = AccListParseData.parseData (record);
          // HACK: Skip loading non-functional accommodations
          if (!accData.isFunctional ())
            continue;
          accList.add (accData);
        }
        if (results.hasNext ()) {
          SingleDataResultSet secondResultSet = results.next ();
          records = secondResultSet.getRecords ();
          while (records.hasNext ()) {
            DbResultRecord record = records.next ();
            accList.getDependencies ().add (AccListParseData.parseDependency (record));
          }
        }
      }
      Collections.sort (accList.getData(), new Comparator<Data> ()
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

  /**
   * This has been replaced by ItemBankService.getGrades.
   *
   * @deprecated use {@link #ItemBankService.getGrades()} instead.
   */
  public List<String> getGrades () throws ReturnStatusException {

    List<TestGrade> testGrades = new ArrayList<TestGrade> ();

    try (SQLConnection connection = getSQLConnection ()) {

      String testKey = null;
      SingleDataResultSet firstResultSet = _studentDll.IB_GetTestGrades_SP (connection, getTdsSettings().getClientName (), testKey, getTdsSettings().getSessionType ());

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

  /**
   * This has been replaced by ItemBankService.getTestForms.
   *
   * @deprecated use {@link #ItemBankService.getTestForms()} instead.
   */
  public List<TestForm> getTestForms (String testID) throws ReturnStatusException {

    List<TestForm> testforms = new ArrayList<TestForm> ();
    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet results = _studentDll.P_GetTestForms_SP (connection, getTdsSettings().getClientName (), testID, getTdsSettings().getSessionType ());

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

  /**
   * This has been replaced by ItemBankService.getItemPath.
   *
   * @deprecated use {@link #ItemBankService.getItemPath()} instead.
   */
  public String getItemPath (long bankKey, long itemKey) throws ReturnStatusException {
    String itemPath = null;

    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet results = _studentDll.IB_GetItemPath_SP (connection, getTdsSettings().getClientName (), bankKey, itemKey);

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

  /**
   * This has been replaced by ItemBankService.getStimulusPath.
   *
   * @deprecated use {@link #ItemBankService.getStimulusPath()} instead.
   */
  public String getStimulusPath (long bankKey, long stimulusKey) throws ReturnStatusException {
    String stimulusPath = null;

    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet results = _studentDll.IB_GetStimulusPath_SP (connection, getTdsSettings().getClientName (), bankKey, stimulusKey);

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
