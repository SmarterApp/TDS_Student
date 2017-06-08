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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.sql.data.AccList;
import tds.student.sql.data.TestForm;
import tds.student.sql.data.TestProperties;
import tds.student.sql.data.TestSegment;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 *         Item Bank Repository Test case
 */
public class ItemBankRepositoryTest extends AbstractTest
{
  private static final Logger _logger        = LoggerFactory.getLogger (ItemBankRepositoryTest.class);

  ItemBankRepository          itemRepository = new ItemBankRepository ();

  // success Test Case
  /**
   * Get All List Test from Item Repository
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testListTests () throws SQLException, ReturnStatusException {
    try {
      List<String> list = itemRepository.listTests ();
      Assert.assertTrue (list.size () > 0);
      if (list != null) {
        _logger.info ("Test List SIZE::" + list.size ());
        for (int i = 0; i < list.size (); i++) {
          _logger.info ("TestProperties Test Id::" + list.get (i));
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

  // failure Test Case
  /**
   * Get All List Test from Item Repository
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testListTestsFailue () throws SQLException, ReturnStatusException {
    try {
      List<String> list = itemRepository.listTests ();
      assertTrue (list == null);
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

  /**
   * Get the Test Properties input Test Key and output TestProperties Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetTestProperties () throws SQLException,
      ReturnStatusException {
    try {
      TestProperties testProperties = itemRepository
          .getTestProperties ("(Oregon)ELPA_6-8-Winter-2012-2013");
      Assert.assertTrue (testProperties != null);
      if (testProperties != null) {
        _logger.info ("TestProperties Test Id::"
            + testProperties.getTestID ());
        _logger.info ("TestProperties Display Name::"
            + testProperties.getDisplayName ());
        _logger.info ("TestProperties is selectable::"
            + testProperties.isSelectable ());
        _logger.info ("TestProperties Validate Completeness::"
            + testProperties.isValidateCompleteness ());
        _logger.info ("TestProperties Acc Family::"
            + testProperties.getAccFamily ());
        _logger.info ("TestProperties Sort Order::"
            + testProperties.getSortOrder ());
        _logger.info ("TestProperties Grade::"
            + testProperties.getGrade ());
        _logger.info ("TestProperties Subject::"
            + testProperties.getSubject ());
        _logger.info ("TestProperties Score By TDS::"
            + testProperties.isScoreByTDS ());
        _logger.info ("TestProperties Max Opportunities::"
            + testProperties.getMaxOpportunities ());
        _logger.info ("TestProperties Min Items::"
            + testProperties.getMinItems ());
        _logger.info ("TestProperties Max Items::"
            + testProperties.getMaxItems ());
        _logger.info ("TestProperties Prefetch::"
            + testProperties.getPrefetch ());

        _logger.info ("TestProperties Segments List Size::"
            + testProperties.getSegments ().size ());

        if (testProperties.getSegments () != null) {
          List<TestSegment> testSegmentList = testProperties
              .getSegments ();
          if (testSegmentList != null && testSegmentList.size () > 0) {
            for (int i = 0; i < testSegmentList.size (); i++) {
              TestSegment testSegment = testSegmentList.get (i);
              _logger.info ("TestProperties Segments Id::"
                  + testSegment.getId ());
              _logger.info ("TestProperties Segments Position::"
                  + testSegment.getPosition ());
              _logger.info ("TestProperties Segments Lable::"
                  + testSegment.getLabel ());
              _logger.info ("TestProperties Segments Permeable::"
                  + testSegment.getIsPermeable ());
              _logger.info ("TestProperties Segments Entry Approval::"
                  + testSegment.getEntryApproval ());
              _logger.info ("TestProperties Segments Exit Approval::"
                  + testSegment.getExitApproval ());
              _logger.info ("TestProperties Segments Item Review::"
                  + testSegment.isItemReview ());
            }

          }
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

  // Failue test case
  /**
   * Get the Test Properties
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetTestPropertiesFailure () throws SQLException,
      ReturnStatusException {
    try {
      TestProperties testProperties = itemRepository
          .getTestProperties (null);

      assertTrue (testProperties == null);

    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp1) {
      exp1.printStackTrace ();
      _logger.error (exp1.getMessage ());
    }
  }

  // Success Test case
  /**
   * Get the Test Accommodations input Test Key and output AccList
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetTestAccommodations () throws SQLException,
      ReturnStatusException {
    try {
      AccList accList = itemRepository
          .getTestAccommodations ("(Oregon)Oregon-Student Help-NA-Winter-2011-2012");
      Assert.assertTrue (accList.getData().size () > 0);
      if (accList != null)
        _logger.info ("SIZE::" + accList.getDependencies ().size ());
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

  // Failure Test case
  /**
   * Get the Test Accommodations
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetTestAccommodationsFailure () throws SQLException,
      ReturnStatusException {
    try {
      AccList accList = itemRepository
          .getTestAccommodations (null);
      assertTrue (accList == null);
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp1) {
      exp1.printStackTrace ();
      _logger.error (exp1.getMessage ());
    }
  }

  /**
   * Get all Grades Output is List of Grades.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetGrades () throws SQLException, ReturnStatusException {
    try {
      List<String> gradesList = itemRepository.getGrades ();
      if (gradesList != null)
        Assert.assertTrue (gradesList.size () > 0);
      if (gradesList != null)
        _logger.info ("Grades List SIZE::" + gradesList.size ());
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

  // Failure test case
  /**
   * Get all Grades Output is List of Grades.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetGradesFailure () throws SQLException, ReturnStatusException {
    try {
      List<String> gradesList = itemRepository.getGrades ();
      assertTrue (gradesList != null);
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

  // success
  /**
   * Get Test Forms for given input Input TestID and output List of Test Forms.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetTestForms () throws SQLException, ReturnStatusException {
    try {
      List<TestForm> testFormsList = itemRepository
          .getTestForms ("OAKS-Writing-HS");
      Assert.assertTrue (testFormsList.size () > 0);
      if (testFormsList != null)
        _logger.info ("Test Forms SIZE::" + testFormsList.size ());
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
  /**
   * 
   * Get Test Forms for given input Input TestID and output List of Test Forms.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetTestFormsFailure () throws SQLException, ReturnStatusException {
    try {
      List<TestForm> testFormsList = itemRepository
          .getTestForms ("OAKS-Writing-HS");
      assertTrue (testFormsList == null);

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
  // Success
  /**
   * get Item Path input long bankKey, long itemKey
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testgetItemPath () throws SQLException, ReturnStatusException {
    try {
      String itempath = itemRepository.getItemPath (157, 9440);
      Assert.assertTrue (itempath != null);
      if (itempath != null)
        _logger.info ("itempath value::" + itempath);
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
  /**
   * get Item Path input long bankKey, long itemKey
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testgetItemPathFailure () throws SQLException, ReturnStatusException {
    try {
      String itempath = itemRepository.getItemPath (1, 9);
      assertTrue (itempath == null);
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

  /**
   * Get the Stimulus path inputs are long bankKey, long stimulusKey and output
   * String stimulusPath
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetStimulusPath () throws SQLException,
      ReturnStatusException {
    try {
      String stimulusPath = itemRepository.getStimulusPath (148, 178);
      Assert.assertTrue (stimulusPath != null);
      if (stimulusPath != null)
        _logger.info ("stimulusPath value::" + stimulusPath);
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

  // Failue Test Case
  /**
   * Get the Stimulus path inputs are long bankKey, long stimulusKey and output
   * String stimulusPath
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetStimulusPathFailue () throws SQLException,
      ReturnStatusException {
    try {
      String stimulusPath = itemRepository.getStimulusPath (1, 1);
      assertTrue (stimulusPath == null);
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
