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
import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import tds.student.services.abstractions.ITestScoringService;
import tds.student.services.data.TestScoreStatus;
import tds.student.sql.repository.ScoringRepository;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Configuration.ITDSSettingsSource;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
//@Configuration
//@ContextConfiguration("classpath:root-context.xml")
public class TestScoringServiceTest extends AbstractTest
{
	private static final Logger _logger = LoggerFactory.getLogger(TestScoringServiceTest.class);
	
	// TODO: Need to configure a Spring-aware test runner and context
	
	@Autowired
	ITDSSettingsSource        tdsSettings;
	
	@Autowired
	ScoringRepository  responseRepository;
  
	@Autowired
	TestScoringService testScoringService;

  // Sucess Test Case
  @Test
  public void testOpportunityItems () throws SQLException, ReturnStatusException {
      String testKey = null;
      String input = null;
      Date testEndDate = null;
      String scoreString = testScoringService.getTestScoreString (testKey, input, testEndDate);
      Assert.assertTrue (scoreString != null);
      if (scoreString != null)
        _logger.info ("Score String ::" + scoreString);
  }

  // Failure Test Case
  @Test
  public void testOpportunityItemsFailure () throws SQLException, ReturnStatusException {
      String testKey = null;
      String input = null;
      Date testEndDate = null;
      String scoreString = testScoringService.getTestScoreString (testKey, input, testEndDate);
      Assert.assertTrue (scoreString == null);
  }

  // Sucess Test Case
  @Test
  public void testCanCompleteTest () throws SQLException, ReturnStatusException {
      UUID oppKey = null;
      String testKey = null;
      boolean scoreString = testScoringService.canCompleteTest (oppKey, testKey);
      Assert.assertTrue (scoreString);
      _logger.info ("Test Failure ::" + scoreString);
  }

  // Failure Test Case
  @Test
  public void testCanCompleteTestFailure () throws SQLException, ReturnStatusException {
      UUID oppKey = null;
      String testKey = null;
      boolean scoreString = testScoringService.canCompleteTest (oppKey, testKey);
      Assert.assertFalse (scoreString);
      _logger.info ("Test Failure ::" + scoreString);
  }

  // Success Test Case
  @Test
  public void testScoreTest () throws SQLException, ReturnStatusException {
	  String OPPKEY = "79CAF065-40F4-43CB-9290-E1B2580AE8DB"; 
	  UUID oppKey = (UUID.fromString(OPPKEY));
      String testKey = "(SBAC_PT)SBAC-ELA-3-Spring-2013-2015";
      TestScoreStatus testScoreStatus = testScoringService.scoreTest (oppKey, testKey);
      Assert.assertTrue (testScoreStatus != null);
      if (testScoreStatus != null)
        _logger.info ("Name values ::" + testScoreStatus.name ());
      _logger.info ("Ordinal Value ::" + testScoreStatus.ordinal ());
  }

  // Failure Test Case
  @Test
  public void testScoreTestFailure () throws SQLException, ReturnStatusException {
	  String OPPKEY = "79CAF065-40F4-43CB-9290-E1B2580AE8DB"; 
	  UUID oppKey = (UUID.fromString(OPPKEY));
      String testKey = "(SBAC_PT)SBAC-ELA-3-Spring-2013-2015";
      TestScoreStatus testScoreStatus = testScoringService.scoreTest (oppKey, testKey);
      Assert.assertTrue (testScoreStatus == null);
  }
  
}
