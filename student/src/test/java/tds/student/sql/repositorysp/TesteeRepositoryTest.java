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
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.sql.data.RTSAccommodation;
import tds.student.sql.data.RTSAttribute;
import tds.student.sql.data.TesteeAttributes;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
public class TesteeRepositoryTest extends AbstractTest
{
  private static final Logger _logger          = LoggerFactory.getLogger (TesteeRepositoryTest.class);

  TesteeRepository            testeeRepository = new TesteeRepository ();

  // Success
  @Test
  public void testLogin () throws SQLException, ReturnStatusException {
    try {
      // String clientname = "Oregon_PT";
      String keyValues = "ID:GUEST;Firstname:JOHN";// "ID:999999932;Firstname:JOHN";
      String sessionID = "GUEST Session";

      TesteeAttributes testeeAttributes = testeeRepository.login (keyValues, sessionID);
      Assert.assertTrue (testeeAttributes != null);
      if (testeeAttributes != null)
        _logger.info ("opportunityStatus Key::" + testeeAttributes.getKey ());
      Iterator<RTSAttribute> rtsatt = testeeAttributes.iterator ();
      while (rtsatt.hasNext ()) {
        RTSAttribute rts = rtsatt.next ();
        _logger.info ("opportunityStatus ID::" + rts.getId ());
        _logger.info ("opportunityStatus Label::" + rts.getLabel ());
        _logger.info ("opportunityStatus Sort Order::" + rts.getSortOrder ());
        _logger.info ("opportunityStatus Value::" + rts.getValue ());
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
  public void testLoginFailure () throws SQLException, ReturnStatusException {
    try {
      // String clientname = "Oregon_PT";
      String keyValues = "ID:GUEST;Firstnafme:JOfHN";// "ID:999999932;Firstname:JOHN";
      String sessionID = "GUEST Session1";
      TesteeAttributes testeeAttributes = testeeRepository.login (keyValues, sessionID);
      assertTrue (testeeAttributes == null);
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
  public void testGetAccommodations () throws SQLException, ReturnStatusException {
    try {
      long testeeKey = 453991;
      List<RTSAccommodation> RTSAccommodationList = testeeRepository.getAccommodations (testeeKey);
      Assert.assertTrue (RTSAccommodationList != null);
      if (RTSAccommodationList != null)
        _logger.info ("RTS Accommodation List Size::" + RTSAccommodationList.size ());
      if (RTSAccommodationList.size () > 0)
        for (int i = 0; i < RTSAccommodationList.size (); i++)
        {
          _logger.info ("Opportunity Items Value Group Id::" + RTSAccommodationList.get (i).getAccCode ());
          _logger.info ("Opportunity Items Value GroupItemsRequired::" + RTSAccommodationList.get (i).getAccFamily ());
        }
      _logger.info ("RTS Accommodation List Value::" + RTSAccommodationList.size ());
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
  public void testGetAccommodationsFailure () throws SQLException, ReturnStatusException {
    try {
      long testeeKey = 45343991;
      List<RTSAccommodation> RTSAccommodationList = testeeRepository.getAccommodations (testeeKey);
      assertTrue (RTSAccommodationList == null);
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
