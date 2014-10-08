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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.sql.data.BrowserCapabilities;
import tds.student.sql.data.ClientLatency;
import tds.student.sql.data.OpportunityAccommodation;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunitySegment;
import tds.student.sql.data.OpportunitySegment.OpportunitySegments;
import tds.student.sql.data.OpportunityStatus;
import tds.student.sql.data.ServerLatency;
import tds.student.sql.data.ServerLatency.OperationType;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestSelection;
import tds.student.sql.data.ToolUsed;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
public class OpportunityRepositoryTest extends AbstractTest
{
  private static final Logger _logger               = LoggerFactory.getLogger (OpportunityRepositoryTest.class);

  OpportunityRepository       opportunityRepository = new OpportunityRepository ();

  // Success Test case
  /**
   * Get the getEligibleTests inputs long testeeKey, UUID sessionKey, String
   * grade output List of TestSelection
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */

  @Test
  public void testGetEligibleTests () throws SQLException,
      ReturnStatusException {
    try {
      UUID uuid = UUID.fromString ("94D18A5A-DF8F-4438-B421-236F5FD07090");
      List<TestSelection> list = opportunityRepository.getEligibleTests (29138,
          uuid, "12");
      Assert.assertTrue (list.size () > 0);
      _logger.info ("SIZE::" + list.size ());
      for (int i = 0; i < list.size (); i++) {
        TestSelection testSelection = list.get (i);
        _logger.info ("testSelection Test Key:" + testSelection.getTestKey ());
        _logger.info ("testSelection Display Name:" + testSelection.getDisplayName
            ());
        _logger.info ("testSelection Grade:" + testSelection.getGrade ());
        _logger.info ("testSelection Max Opp:" + testSelection.getMaxOpportunities
            ());
        _logger.info ("testSelection Mode:" + testSelection.getMode ());
        _logger.info ("testSelection Opp:" + testSelection.getOpportunity ());
//        _logger.info ("testSelection Reason:" + testSelection.getReason ());
        _logger.info ("testSelection Test Id:" + testSelection.getTestID ());
        _logger.info ("testSelection Test Key:" + testSelection.getTestKey ());
        _logger.info ("testSelection Opp Key:" + testSelection.getOppKey ());
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

  // Failure Test Case
  /**
   * Get the getEligibleTests inputs long testeeKey, UUID sessionKey, String
   * grade output List of TestSelection
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetEligibleTestsFailure () throws SQLException,
      ReturnStatusException {
    try {
      UUID uuid = UUID.fromString ("94D18A5A-DF8F-4438-B421-236F5FD07090");
      List<TestSelection> list = opportunityRepository.getEligibleTests (29138,
          uuid, "12");
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

  // Success Test Case
  /**
   * Test Approve Accommodations inputs opportunity Instance, segment,
   * segmentAccommodations output ReturnStatus
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testApproveAccommodations () throws SQLException, ReturnStatusException {
    try {
      // UUID oppKey = UUID.fromString ("FCEC4C40-0556-42D0-BF42-7A64C491AD56");
      // UUID sessionKey = UUID.fromString
      // ("C19F428D-4395-4B65-A567-6D418711A2DC");
      // UUID browserKey = UUID.fromString
      // ("5DCE7D2A-E724-4930-AC9E-B0F2FD4D263D");
      // int segment = 0;
      // String segmentAccoms = "TDS_BP1";
      UUID oppKey = UUID.fromString ("FD4C3B16-2D78-4234-A97A-F3168237B478");
      UUID sessionKey = UUID.fromString ("B717F914-6C57-414B-A875-CBC3286A4A8A");
      UUID browserKey = UUID.fromString ("A8B90459-DFD0-4551-880A-DC00633CDCF8");
      int segment = 0;
      String segmentAccoms = "TDS_BP1";
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppKey, sessionKey, browserKey);
      ReturnStatus returnStatus = opportunityRepository.approveAccommodations (opportunityInstance, segment, segmentAccoms);
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

  // Failure Test Case with status and reason
  /**
   * Test Approve Accommodations inputs opportunity Instance, segment,
   * segmentAccommodations output ReturnStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testApproveAccommodationsFailue () throws SQLException, ReturnStatusException {
    try {

      UUID oppKey = UUID.fromString ("FD4C3dfB16-2D78-4234-Af97A-F3168237B478");
      UUID sessionKey = UUID.fromString ("B71fdF914-6C57-414B-A87f5-CBC3286A4A8A");
      UUID browserKey = UUID.fromString ("A8B90fd459-DFD0-4551-8f80A-DC00633CDCF8");
      int segment = 1;
      String segmentAccoms = "T";
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppKey, sessionKey, browserKey);
      ReturnStatus returnStatus = opportunityRepository.approveAccommodations (opportunityInstance, segment, segmentAccoms);
      assertTrue (returnStatus != null);
      if (returnStatus != null) {
        System.err.println (String.format ("Status: %s", returnStatus.getStatus ()));
        System.err.println (String.format ("Reason: %s", returnStatus.getReason ()));
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
  /**
   * Test Approve Accommodations Failure Test Case inputs opportunity Instance,
   * segment, segmentAccommodations output ReturnStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testApproveAccommodationsFailure () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("FCEC4C40-0556-42D0-BF42-7Ah64C491AD56");
      UUID sessionKey = UUID.fromString ("C19F428D-4395-4B65-Ah567-6D418711A2DC");
      UUID browserKey = UUID.fromString ("5DCE7D2A-E724-4930-ACh9E-B0F2FD4D263D");
      int segment = 0;
      String segmentAccoms = "TDS_BP1";

      OpportunityInstance opportunityInstance = new OpportunityInstance (oppKey,
          sessionKey, browserKey);
      ReturnStatus returnStatus = opportunityRepository.approveAccommodations
          (opportunityInstance, segment, segmentAccoms);
      Assert.assertTrue (returnStatus != null);
      if (returnStatus != null)
        _logger.info ("Return Status Value::" + returnStatus.getStatus ());

      assertTrue ("failed".equalsIgnoreCase (returnStatus.getStatus ()));
      assertTrue (("Student can only self-approve unproctored sessions[-----]").equalsIgnoreCase
          (returnStatus.getReason ()));
      assertTrue ("T_ApproveAccommodations".equalsIgnoreCase (returnStatus.getContext
          ()));
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

  // Success Test Case
  /**
   * Test Open Test Opportunity inputs testeeKey, testkey, sessionKey,
   * browserKey output opportunityInfo Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testOpenTestOpportunity () throws SQLException,
      ReturnStatusException {
    try {
      long testeeKey = 29138;
      String testkey = "(Oregon)OAKS-Math-3-Fall-2012-2013";
      UUID sessionKey = UUID.fromString ("D09CC4F9-FF95-4679-A441-0115E1BCFB7C");
      UUID browserKey = UUID.fromString ("3AEF9213-85E6-4346-A202-A381B5334CD5");
      OpportunityInfo opportunityInfo = opportunityRepository.openTestOpportunity
          (testeeKey, testkey, sessionKey, browserKey);
      Assert.assertTrue (opportunityInfo != null);
      if (opportunityInfo != null)
      {
        _logger.info ("OpportunityInfo Oppotunity key Value::" +
            opportunityInfo.getOppKey ());
        _logger.info ("OpportunityInfo Browser key Value::" +
            opportunityInfo.getBrowserKey ());
        _logger.info ("OpportunityInfo Status Value::" + opportunityInfo.getStatus
            ());
      }
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception e) {
      e.printStackTrace ();
      _logger.error (e.getMessage ());
    }
  }

  // run below script each time before the running the test case
  // update TestOpportunity
  // set status = 'approved'
  // , prevstatus = 'started'
  // , datestarted = getdate()
  // where _fk_session = 'C91FF07A-5F44-4446-BEA4-E1D7AA4FC8CC'
  // and _fk_browser = 'A10304D5-B1EA-4408-B068-5D8EC28ACA71'
  /**
   * Test start Test Opportunity inputs oppKey, sessionKey, browserId output
   * TestConfig Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testStartTestOpportunity () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("E995CA55-1DAA-48AB-AB50-D0623B0C1427");
      UUID sessionKey = UUID.fromString ("C91FF07A-5F44-4446-BEA4-E1D7AA4FC8CC");
      UUID browserId = UUID.fromString ("A10304D5-B1EA-4408-B068-5D8EC28ACA71");

      OpportunityInstance OpportunityInstance = new OpportunityInstance (oppKey,
          sessionKey, browserId);
      TestConfig testConfig = opportunityRepository.startTestOpportunity
          (OpportunityInstance, null, null);
      if (testConfig != null) {
        _logger.info ("TestConfig Start Position ::" + testConfig.getStartPosition
            ());
        _logger.info ("TestConfig Status::" + testConfig.getStatus ());

        _logger.info ("TestConfig REstart::" + testConfig.getRestart ());
        _logger.info ("TestConfig Test Length::" + testConfig.getTestLength ());
        _logger.info ("TestConfig Interface Timeout::"
            + testConfig.getInterfaceTimeout ());
        _logger.info ("TestConfig Restart Mins ::" + testConfig.getOppRestartMins
            ());
        _logger.info ("TestConfig Content Load Timeout::"
            + testConfig.getContentLoadTimeout ());
        _logger.info ("TestConfig Request Interface Timeout::"
            + testConfig.getRequestInterfaceTimeout ());
        _logger.info ("TestConfig Start Position ::" + testConfig.getStartPosition
            ());
        _logger.info ("TestConfig Prefetch::" + testConfig.getPrefetch ());
        _logger.info ("TestConfig ScoreBy TDS::" + testConfig.isScoreByTDS ());
        _logger.info ("TestConfig Validate Completeness ::"
            + testConfig.isValidateCompleteness ());
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

  // Failure Test Case
  /**
   * Test start Test Opportunity inputs OpportunityInstance, null, null output
   * TestConfig Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testStartTestOpportunityFailure () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppKey = UUID.fromString ("E995CA55-1DAA-48AB-AB50-D0623B0C1428");
      UUID sessionKey = UUID.fromString ("C91FF07A-5F44-4446-BEA4-E1D7AA4FC8CD");
      UUID browserId = UUID.fromString ("A10304D5-B1EA-4408-B068-5D8EC28ACA72");

      OpportunityInstance OpportunityInstance = new OpportunityInstance (oppKey,
          sessionKey, browserId);
      TestConfig testConfig = opportunityRepository.startTestOpportunity
          (OpportunityInstance, null, null);
      if (testConfig == null) {
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

  /**
   * Test Test Get Opportunity Segments inputs OpportunityInstance output
   * OpportunitySegments Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetOpportunitySegments () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("9D7AA6E1-60E3-4F33-99EF-DD0330B75F06");
      UUID session = null;
      UUID browser = null;
      OpportunityInstance OpportunityInstance = new OpportunityInstance (oppkey,
          session, browser);
      OpportunitySegments opportunitySegments =
          opportunityRepository.getOpportunitySegments (OpportunityInstance);
      Assert.assertTrue (opportunitySegments != null);
      if (opportunitySegments != null)
      {
        for (OpportunitySegment opportunitySegment : opportunitySegments) {
          _logger.info (opportunitySegment.getKey ());
          _logger.info (opportunitySegment.getFormKey ());
          _logger.info (opportunitySegment.getFormID ());
          _logger.info (String.valueOf (opportunitySegment.getIsPermeable ()));
          _logger.info (opportunitySegment.getRestorePermOn ());
          _logger.info (opportunitySegment.getFtItems ());
        }
      }
      _logger.info ("OpportunityInfo Browser key Value::" +
          opportunitySegments.get (0));
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

  // Failure Test Case
  /**
   * Test Test Get Opportunity Segments inputs OpportunityInstance output
   * OpportunitySegments Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetOpportunitySegmentsFailure () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("9D7AA6E1-60E3-4F33-99EF-DD0330B75F07");
      UUID session = null;
      UUID browser = null;
      OpportunityInstance OpportunityInstance = new OpportunityInstance (oppkey,
          session, browser);
      OpportunitySegments opportunitySegments =
          opportunityRepository.getOpportunitySegments (OpportunityInstance);
      assertTrue (opportunitySegments == null);
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
   * Test Test Get Opportunity Segments inputs uuid output OpportunitySegments
   * Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetOpportunitySegmentsWithUuid () throws SQLException,
      ReturnStatusException {
    try {
      UUID uuid = UUID.fromString ("9D7AA6E1-60E3-4F33-99EF-DD0330B75F06");
      OpportunitySegments opportunitySegments =
          opportunityRepository.getOpportunitySegments (uuid);
      _logger.info ("OpportunityInfo Browser key Value::" +
          opportunitySegments.get (0));
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
   * Test Test Get Opportunity Segments inputs uuid output OpportunitySegments
   * Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetOpportunitySegmentsWithUuidFailure () throws SQLException,
      ReturnStatusException {
    try {
      UUID uuid = UUID.fromString ("9D7AA6E1-60E3-4F33-99EF-DD0330B75F06");
      OpportunitySegments opportunitySegments =
          opportunityRepository.getOpportunitySegments (uuid);
      _logger.info ("OpportunityInfo Browser key Value::");
      assertTrue (opportunitySegments == null);
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
   * Test Get Opportunity Accommodations inputs opportunityInstance Object
   * output List of OpportunityAccommodation Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetOpportunityAccommodations () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("9D7AA6E1-60E3-4F33-99EF-DD0330B75F06");
      UUID session = null;
      UUID browser = null;
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browser);
      List<OpportunityAccommodation> OpportunityAccommodationList =
          opportunityRepository.getOpportunityAccommodations (opportunityInstance,
              null);
      Assert.assertTrue (OpportunityAccommodationList != null);
      if (OpportunityAccommodationList != null)
      {
        _logger.info ("OpportunityAccommodationList list Size::" +
            OpportunityAccommodationList.size ());
        for (OpportunityAccommodation opportunityAccommodation : OpportunityAccommodationList) {
          _logger.info (opportunityAccommodation.getAccCode ());
          _logger.info (opportunityAccommodation.getAccType ());
          _logger.info (String.valueOf (opportunityAccommodation.getSegmentPosition ()));
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

  // Failure Test Case
  /**
   * Test Get Opportunity Accommodations inputs opportunityInstance Object
   * output List of OpportunityAccommodation Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetOpportunityAccommodationsFailure () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("9D7AA6E1-60E3-4F33-99EF-DD0330B75F07");
      UUID session = null;
      UUID browser = null;
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browser);
      List<OpportunityAccommodation> OpportunityAccommodationList =
          opportunityRepository.getOpportunityAccommodations (opportunityInstance,
              null);
      assertTrue (OpportunityAccommodationList == null);
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
  // delete from OpportunityClient where _fk_TestOpportunity =
  // '68901FC9-38CE-4C85-A1A6-747F9468B70C';
  // BEGIN; SET NOCOUNT ON; exec T_ValidateAccess
  // '9d8ff89a-00a7-44d3-837c-00239c89edc2',
  // '203e652d-436e-474c-8f22-fea573e15add',
  // 'eeae33ee-2531-40af-99f0-cfede75366d0'; end;
  /**
   * Test Get Opportunity Accommodations inputs opportunityInstance Object
   * output List of OpportunityAccommodation Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testLogOpportunityClient () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("68901FC9-38CE-4C85-A1A6-747F9468B70C");
      UUID session = UUID.fromString ("9D7AA6E1-60E3-4F33-99EF-DD0330B75F06");
      UUID browser = UUID.fromString ("46D352E2-FC73-4BD2-9FE7-6EF13BE75FE2");

      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browser);
      BrowserCapabilities browserCapabilities = new BrowserCapabilities ();
      browserCapabilities.setClientIP ("192.168.15.7");
      browserCapabilities.setProxyIP (null);
      browserCapabilities.setUserAgent
          ("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0");
      browserCapabilities.setScreenRez ("1680x1050");
      browserCapabilities.setIsSecure (false);
      browserCapabilities.setMacAddress (null);
      browserCapabilities.setLocalIP (null);

      opportunityRepository.logOpportunityClient (opportunityInstance, 0,
          browserCapabilities);
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
   * Test Log Opportunity Client Failure inputs opportunityInstance Object
   * output void
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testLogOpportunityClientFailure () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("68901FC9-38CE-4C85-A1A6-747F9468B70D");
      UUID session = UUID.fromString ("9D7AA6E1-60E3-4F33-99EF-DD0330B75F07");
      UUID browser = UUID.fromString ("46D352E2-FC73-4BD2-9FE7-6EF13BE75FE3");

      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browser);
      BrowserCapabilities browserCapabilities = new BrowserCapabilities ();
      browserCapabilities.setClientIP ("192.168.15.7");
      browserCapabilities.setProxyIP (null);
      browserCapabilities.setUserAgent
          ("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0");
      browserCapabilities.setScreenRez ("1680x1050");
      browserCapabilities.setIsSecure (false);
      browserCapabilities.setMacAddress (null);
      browserCapabilities.setLocalIP (null);

      opportunityRepository.logOpportunityClient (opportunityInstance, 0,
          browserCapabilities);
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
   * Test validate Access inputs opportunityInstance Object output
   * OpportunityStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testValidateAccess () throws SQLException,
      ReturnStatusException {
    try {

      UUID oppkey = UUID.fromString ("9D8FF89A-00A7-44D3-837C-00239C89EDC2");
      UUID session = UUID.fromString ("203E652D-436E-474C-8F22-FEA573E15ADD");
      UUID browserID = UUID.fromString ("EEAE33EE-2531-40AF-99F0-CFEDE75366D0");

      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browserID);
      OpportunityStatus opportunityStatus = opportunityRepository.validateAccess
          (opportunityInstance);
      Assert.assertTrue (opportunityStatus != null);
      if (opportunityStatus != null)
      {
        _logger.info ("opportunityStatus status::" + opportunityStatus.getStatus
            ());
        _logger.info ("opportunityStatus status::" + opportunityStatus.getComment
            ());
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

  // FAilure Test CAse
  /**
   * Test validate Access inputs opportunityInstance Object output
   * OpportunityStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testValidateAccessFailure () throws SQLException,
      ReturnStatusException {
    try {

      UUID oppkey = UUID.fromString ("9D8FF89A-00A7-44D3-837C-00239C89EDC2");
      UUID session = UUID.fromString ("203E652D-436E-474C-8F22-FEA573E15ADD");
      UUID browserID = UUID.fromString ("EEAE33EE-2531-40AF-99F0-CFEDE75366D0");

      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browserID);
      OpportunityStatus opportunityStatus = opportunityRepository.validateAccess
          (opportunityInstance);
      assertTrue (opportunityStatus == null);
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
   * Test set Status inputs oppkey, status, comment Object output
   * OpportunityStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testSetStatus () throws SQLException, ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("72FC4289-8141-47EB-AA5D-008ADB6EE395");
      String status = "paused";
      String comment = null;

      ReturnStatus returnStatus = opportunityRepository.setStatus (oppkey,
          status, comment);
      Assert.assertTrue (returnStatus.getStatus ().equalsIgnoreCase ("paused"));
      if (returnStatus != null)
        _logger.info ("opportunityStatus status::" + returnStatus.getStatus ());
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

  // FAilure Test CAse
  /**
   * Test set Status inputs oppkey, status, comment Object output ReturnStatus
   * Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testSetStatusFailure () throws SQLException, ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("72FC4289-8141-47EB-AA5D-008ADB6EE396");
      String status = "paused";
      String comment = null;

      ReturnStatus returnStatus = opportunityRepository.setStatus (oppkey,
          status, comment);
      assertTrue (returnStatus == null);
      _logger.info ("Success:" + returnStatus.getStatus ());
      _logger.info ("Reason:" + returnStatus.getReason ());
      if (returnStatus != null)
        _logger.info ("opportunityStatus status::" + returnStatus.getStatus ());
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
   * Test Status With Validation inputs oppkey,session, browserID Object output
   * ReturnStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testSetStatusWithValidation () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("6B25C12D-0456-4E99-B6E4-C2A91DCA940B");
      UUID session = UUID.fromString ("906113D4-A60F-4AE7-80FA-79F781BE9109");
      UUID browserID = UUID.fromString ("EC752ACC-DF56-4B22-8EDA-B7064F59FDAA");
      String status = "rescored";
      String comment = null;
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browserID);
      ReturnStatus returnStatus = opportunityRepository.setStatusWithValidation
          (opportunityInstance, status, comment);
      Assert.assertTrue (returnStatus.getStatus ().equalsIgnoreCase
          ("rescored"));
      if (returnStatus != null)
        _logger.info ("opportunityStatus status::" + returnStatus.getStatus ());
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
   * Test set Status With Validation inputs oppkey, session, browserID Object
   * output ReturnStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testSetStatusWithValidationFailure () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("6B25C12D-0456-4E99-B6E4-C2A91DCA940B");
      UUID session = UUID.fromString ("906113D4-A60F-4AE7-80FA-79F781BE9109");
      UUID browserID = UUID.fromString ("EC752ACC-DF56-4B22-8EDA-B7064F59FDAA");
      String status = "rescored";
      String comment = null;
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browserID);
      ReturnStatus returnStatus = opportunityRepository.setStatusWithValidation
          (opportunityInstance, status, comment);
      assertTrue (returnStatus != null);
      {
        _logger.error ("Status: " + returnStatus.getStatus ());
        _logger.error ("Reason: " + returnStatus.getReason ());
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

  /**
   * Test record Server Latency inputs opportunityInstance, serverLatency Object
   * output integer
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testRecordServerLatencyFailure () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("F4E3EA6C-301D-448F-854C-2899A897B2B2");
      UUID session = UUID.randomUUID ();
      UUID browserID = UUID.randomUUID ();
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browserID);
      ServerLatency serverLatency = new ServerLatency ();
      serverLatency.setDbLatency (4);
      serverLatency.setPageList (null);
      serverLatency.setItemList (null);
      serverLatency.setOperation (OperationType.Unknown);
      int serverlatency = opportunityRepository.recordServerLatency
          (opportunityInstance, serverLatency);
      assertTrue (serverlatency == 0);
      _logger.info ("serverlatency value::" + serverlatency);
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
   * Test Record Client Latency inputs opportunityInstance, serverLatency Object
   * output integer
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testRecordClientLatency () throws SQLException,
      ReturnStatusException {
    try {

      UUID oppkey = UUID.fromString ("29A51278-BC85-4887-88CF-7457CF2C5421");
      UUID session = UUID.fromString ("F4E3EA6C-301D-448F-854C-2899A897B2B2");
      UUID browserID = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
      int itempage = 1;
      int numitems = 2;
      int visitCount = 3;
      Date createDate = null;
      Date loadDate = null;
      int loadTime = 40;
      int requestTime = 50;
      int visitTime = 60;
      Integer loadAttempts = null;
      // Date visitDate = null;
      String toolsUsed =
          "TDS_CalcSci&TDS_CalcGraphing&TDS_CalcRegress&TDS_CalcMatrices;TDS_FT_San-Serif;TDS_SC0;";

      ClientLatency clientLatency = new ClientLatency ();
      clientLatency.setItemPage (itempage);
      clientLatency.setNumItems (numitems);
      clientLatency.setVisitCount (visitCount);
      clientLatency.setCreateDate (createDate);
      clientLatency.setLoadTime (loadTime);
      clientLatency.setLoadDate (loadDate);
      clientLatency.setRequestTime (requestTime);
      clientLatency.setVisitTime (visitTime);
      clientLatency.setLoadAttempts (loadAttempts);
      clientLatency.setToolsUsed (toolsUsed);

      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browserID);
      int clientLatencyval = opportunityRepository.recordClientLatency
          (opportunityInstance, clientLatency);
      Assert.assertTrue (clientLatencyval == 0);
      _logger.info ("ClientLatency value::" + clientLatencyval);
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
   * Test Record Client Latency inputs opportunityInstance, clientLatencyList
   * Object output integer
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */

  @Test
  public void testRecordClientLatencies () throws SQLException,
      ReturnStatusException {
    try {

      // SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
      // String dateInString = "07-06-2013";
      // Date date = formatter.parse(dateInString);
      DateFormat visitdateFormat = new
          SimpleDateFormat ("yyyy-mm-dd HH:mm:ss.SSS");
      Date createdDate = visitdateFormat.parse ("2012-05-29 18:53:29.457");
      Date loadDate = visitdateFormat.parse ("2012-05-29 18:53:29.457");
      Date visitdate = visitdateFormat.parse ("2012-05-29 14:53:52.847");
      // Date d1 = new Date ();
      UUID oppkey = UUID.fromString ("445D32A4-EEE0-4CA2-9645-B411EAE18AC1");
      UUID session = UUID.fromString ("502165F2-909C-4FFB-8AB1-0EB2A398C8FF");
      UUID browserID = UUID.fromString ("1FEB0DBA-CAC0-48DE-AAB5-AEC566DD15F1");
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browserID);
      ClientLatency clientLatency = new ClientLatency ();
      clientLatency.setItemPage (99);
      clientLatency.setNumItems (1);
      clientLatency.setVisitCount (1);

      clientLatency.setCreateDate (createdDate);
      clientLatency.setLoadDate (loadDate);
      clientLatency.setRequestTime (5392);
      clientLatency.setVisitTime (2485);
      clientLatency.setLoadAttempts (0);
      clientLatency.setLoadTime (5358);
      clientLatency.setToolsUsed (null);
      clientLatency.setVisitDate (visitdate);
      List<ClientLatency> clientLatencyList = new ArrayList<ClientLatency> ();
      clientLatencyList.add (clientLatency);
      int clientLatencyval = opportunityRepository.recordClientLatencies
          (opportunityInstance, clientLatencyList);
      Assert.assertTrue (clientLatencyval == 0);
      _logger.info ("ClientLatency value::" + clientLatencyval);
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
   * Test Record Tools Used inputs oppkey, toolUsedList Object output integer
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testRecordToolsUsed () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("445D32A4-EEE0-4CA2-9645-B411EAE18AC1");
      List<ToolUsed> toolUsedList = new ArrayList<ToolUsed> ();
      ToolUsed toolUsed = new ToolUsed ();
      toolUsed.setPage (1);
      toolUsed.setType ("TestCase");
      toolUsed.setCode ("TestCase_1");
      toolUsedList.add (toolUsed);
      int toolUsedVal = opportunityRepository.recordToolsUsed (oppkey,
          toolUsedList);
      Assert.assertTrue (toolUsedVal == 0);
      _logger.info ("ToolUsed value::" + toolUsedVal);
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
   * Test Submit Request inputs opportunityInstance, itempage, itemposition,
   * requestType, requestValue, requestParameters, requestDescription Object
   * output integer
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testSubmitRequest () throws SQLException, ReturnStatusException
  {
    try {
      UUID oppkey = UUID.fromString ("17062A57-B857-451D-B817-8BEC587787CE");
      UUID session = UUID.fromString ("18597734-367C-4F90-B2C6-92A36615481C");
      UUID browserID = null;
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browserID);
      int itempage = 16;
      int itemposition = 16;
      String requestType = "EMBOSSITEM";
      String requestValue =
          "D:\\DataFiles\\BB_Files\\tds_airws_org\\TDSCore_2012-2013\\Bank-104\\Items\\Item-104-37527\\item_37527_enu_nemeth.brf";
      String requestParameters = "FileFormat:BRF";
      String requestDescription = "Item 16 (BRF)";
      int clientLatencyval = opportunityRepository.submitRequest
          (opportunityInstance, itempage, itemposition, requestType, requestValue,
              requestParameters, requestDescription);
      Assert.assertTrue (clientLatencyval == 0);
      _logger.info ("ClientLatency value::" + clientLatencyval);
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

  // update TestOpportunity
  // set status = 'started'
  // , prevstatus = 'approved'
  // , datestarted = getdate()
  // , waitingForSegment = null
  // where _fk_session = '8B758F90-E048-4FC7-B4E4-C08DD8A66AC9'
  // and _fk_browser = '6018A101-464C-41FC-94F3-798C5DB02797'
  /**
   * Test Wait For Segment inputs opportunityInstance, int, int, boolean Object
   * output ReturnStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testWaitForSegment () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("B54DDDF6-80D9-4D22-98CA-0006EBB09710");
      UUID session = UUID.fromString ("8B758F90-E048-4FC7-B4E4-C08DD8A66AC9");
      UUID browserID = UUID.fromString ("6018A101-464C-41FC-94F3-798C5DB02797");
      int segment = 2;
      boolean entry = false;
      ;
      boolean exit = true;
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browserID);
      ReturnStatus returnstatus = opportunityRepository.waitForSegment
          (opportunityInstance, segment, entry, exit);
      Assert.assertTrue (returnstatus != null);
      _logger.info ("ReturnStatus Status::" + returnstatus.getStatus ());
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

  // Failure Test CAse
  /**
   * Test Wait For Segment inputs opportunityInstance, int, int, boolean Object
   * output ReturnStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testWaitForSegmentFailure () throws SQLException,
      ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("B54DDDF6-80D9-4D22-98CA-0006EBB09711");
      UUID session = UUID.fromString ("8B758F90-E048-4FC7-B4E4-C08DD8A66AC8");
      UUID browserID = UUID.fromString ("6018A101-464C-41FC-94F3-798C5DB02797");
      int segment = 2;
      boolean entry = false;
      ;
      boolean exit = true;
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browserID);
      ReturnStatus returnstatus = opportunityRepository.waitForSegment
          (opportunityInstance, segment, entry, exit);
      assertTrue (returnstatus != null);
      _logger.info ("ReturnStatus Status::" + returnstatus.getStatus ());
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
   * Test Exit Segment Failuere inputs opportunityInstance, segment Object
   * output ReturnStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  // Failure
  @Test
  public void testExitSegmentFailuere () throws SQLException, ReturnStatusException {
    try {
      UUID oppkey = UUID.fromString ("9F1DCD39-111A-4417-8464-00A0E1291E4E");
      UUID session = UUID.fromString ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
      UUID browserID = UUID.fromString ("99CDB138-17B3-4DFA-B892-D4E0060FD477");
      int segment = 1;
      OpportunityInstance opportunityInstance = new OpportunityInstance (oppkey,
          session, browserID);
      ReturnStatus returnstatus = opportunityRepository.exitSegment
          (opportunityInstance, segment);
      assertTrue (returnstatus != null);
      if (returnstatus != null) {
        _logger.info ("ReturnStatus Status::" + returnstatus.getStatus ());
        assertTrue ("failed".equalsIgnoreCase (returnstatus.getStatus ()));
        assertTrue ("The session keys do not match; please consult your test administrator [-----]".equalsIgnoreCase (returnstatus.getReason ()));

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

  /**
   * Test Record Comment inputs sessionKey, long, oppkey, String Object output
   * void
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testRecordComment () throws SQLException, ReturnStatusException
  {
    try {
      UUID sessionKey = UUID.fromString ("0A94BDC9-86E7-43B7-82FD-4CDB0AF08EC2");
      long testee = -238;
      UUID oppkey = UUID.fromString ("F4E3EA6C-301D-448F-854C-2899A897B2B2");
      String comment = "Test Comment";
      opportunityRepository.recordComment (sessionKey, testee, oppkey, comment);
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
   * Test get Comment inputs uuid Object output ReturnStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetComment () throws SQLException, ReturnStatusException {
    try {
      UUID uuid = UUID.fromString ("F4E3EA6C-301D-448F-854C-2899A897B2B2");
      String commmentVal = opportunityRepository.getComment (uuid);
      _logger.info ("commment Value::" + commmentVal);
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
   * Test get Comment inputs uuid Object output ReturnStatus Object
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetCommentFailure () throws SQLException, ReturnStatusException {
    try {
      UUID uuid = UUID.fromString ("F4E3EA6C-301D-448F-854C-2899A897B2B2");
      String commmentVal = opportunityRepository.getComment (uuid);
      _logger.info ("commment Value::" + commmentVal);
      assertTrue (commmentVal == null);
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
