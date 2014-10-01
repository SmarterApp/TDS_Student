/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repositorysp;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.test.framework.AbstractTest;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;


/**
 * @author temp_rreddy
 * 
 */
public class ConfigLoaderTest extends AbstractTest
{
  private static final Logger _logger = LoggerFactory.getLogger(ConfigLoaderTest.class);
  TDSSettings  tdsSettings = new TDSSettings ();
  ConfigLoader dao         = new ConfigLoader ();

  // Success Test Case
  /**
   * Get all client names. No input parameters. Test case is true if the
   * client's list size greater than 0.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetClients () throws SQLException, ReturnStatusException {
    try {
      List<String> clientsList = dao.getClients ();
      Assert.assertTrue (clientsList.size () > 0);
      if (clientsList != null)
        _logger.info ("SIZE::" + clientsList.size ());
      for (String clients : clientsList) {
        _logger.info ("Client Names::" + clients);
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
   * Test case if fail if the Client size is null
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testGetClientsFailue () throws SQLException, ReturnStatusException {
    try {
      List<String> clientsList = dao.getClients ();
      assertTrue(clientsList == null);
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
   * Load the externs. It returns void
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testloadExterns () throws SQLException, ReturnStatusException {
    try {
      dao.loadExterns ();
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
   * Load the PTSetup. It returns void
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testloadPTSetup () throws SQLException, ReturnStatusException {
    try {
      dao.loadPTSetup ();
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
   * It has load the Login Requirements.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testLoadLoginRequirements () throws SQLException, ReturnStatusException {
    try {
      dao.loadLoginRequirements ();
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
   * Load the Forbidden Apps. It returns void
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testLoadForbiddenApps () throws SQLException, ReturnStatusException {
    try {
      dao.loadForbiddenApps ();
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
   * Load the voice packs. It returns void
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testLoadVoicePacks () throws SQLException, ReturnStatusException {
    try {
      dao.loadVoicePacks ();
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
   * Load the Global Accommodations. It returns void
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testLoadGlobalAccommodations () throws SQLException, ReturnStatusException {
    try {
      dao.loadGlobalAccommodations ();
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
   * Load the Browser Rules. It returns void
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testLoadBrowserRules () throws SQLException, ReturnStatusException {
    try {
      dao.loadExterns ();
      dao.loadBrowserRules ();
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
   * Load the Network Diagnostics. It returns void
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testLoadNetworkDiagnostics () throws SQLException, ReturnStatusException {
    try {
      dao.loadNetworkDiagnostics ();
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
   * Load the Application Settings. It returns void
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   *           Caught the ReturnStatusException and Exception
   */
  @Test
  public void testloadAppSettings () throws SQLException, ReturnStatusException {
    try {
      dao.loadExterns ();
      dao.loadAppSettings ();
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
