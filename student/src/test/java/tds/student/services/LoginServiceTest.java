/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.student.services.data.LoginInfo;
import tds.student.services.data.LoginKeyValues;
import tds.student.sql.data.Accommodations;
import tds.student.sql.repository.SessionRepository;
import tds.student.sql.repository.TesteeRepository;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
public class LoginServiceTest extends AbstractTest
{
  private static final Logger _logger           = LoggerFactory.getLogger (LoginServiceTest.class);

  // TODO: Add Spring configuration
  SessionRepository           sessionRepository = null;
  @Autowired
  LoginService      loginService;

  // Suceess Test Case
  @Test
  public void testLogin () throws SQLException, ReturnStatusException {
    String sessionID = "";
    LoginKeyValues keyValues = null;
    LoginInfo loginInfo = loginService.login (sessionID, keyValues);
    Assert.assertTrue (loginInfo.getSession () != null);
    if (loginInfo != null)
      _logger.info ("SIZE::" + loginInfo.getSession ());
    _logger.info ("SIZE::" + loginInfo.getTestee ());
  }

  // Failure Test Case
  @Test
  public void testLoginFailure () throws SQLException, ReturnStatusException {
    String sessionID = "";
    LoginKeyValues keyValues = null;
    LoginInfo loginInfo = loginService.login (sessionID, keyValues);
    Assert.assertTrue (loginInfo.getSession () != null);
    if (loginInfo != null)
      _logger.info ("SIZE::" + loginInfo.getValidationErrors ());
  }

  // Suceess Test Case
  @Test
  public void testSetRTSAccommodations () throws SQLException, ReturnStatusException {
    long testee = 0;
    String accFamily = null;
    Accommodations accommodations = null;
    loginService.setRTSAccommodations (testee, accFamily, accommodations);
  }

  // Failure Test Case
  @Test
  public void testSetRTSAccommodationsFailure () throws SQLException, ReturnStatusException {
    long testee = 0;
    String accFamily = null;
    Accommodations accommodations = null;
    loginService.setRTSAccommodations (testee, accFamily, accommodations);
  }

}
