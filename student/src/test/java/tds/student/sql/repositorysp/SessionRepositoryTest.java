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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.sql.data.TestSession;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
public class SessionRepositoryTest extends AbstractTest
{

  private static final Logger _logger           = LoggerFactory.getLogger (SessionRepositoryTest.class);

  SessionRepository           sessionRepository = new SessionRepository ();

  // Success
  @Test
  public void testgetSession () throws SQLException, ReturnStatusException {
    try {
      String sessionID = "Mercury-9";
      TestSession testSession = sessionRepository.getSession (sessionID);
      Assert.assertTrue (testSession != null);

      _logger.info ("TestSession Id::" + testSession.getId ());
      _logger.info ("TestSession Name::" + testSession.getName ());
      _logger.info ("TestSession Need Approval::" + testSession.getNeedApproval ());
      _logger.info ("TestSession Browser Key::" + testSession.getBrowserKey ());
      _logger.info ("TestSession Date Begin::" + testSession.getDateBegin ());
      _logger.info ("TestSession Date Created::" + testSession.getDateCreated ());
      _logger.info ("TestSession Date End::" + testSession.getDateEnd ());
      _logger.info ("TestSession Key::" + testSession.getKey ());
      _logger.info ("TestSession Status Type::" + testSession.getStatusType ());

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
  public void testgetSessionFailure () throws SQLException, ReturnStatusException {
    try {
      String sessionID = "Mercury-11";
      TestSession testSession = sessionRepository.getSession (sessionID);
      // assertTrue(testSessionList == null);
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
