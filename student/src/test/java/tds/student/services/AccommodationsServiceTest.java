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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.student.services.data.OpenTestAcc;
import tds.student.sql.data.Accommodations;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.ItemBankRepository;
import tds.student.sql.repository.OpportunityRepository;
import tds.student.sql.repository.TesteeRepository;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
// TODO: Needs to be configured with Spring environment
public class AccommodationsServiceTest extends AbstractTest
{
  private static final Logger _logger               = LoggerFactory.getLogger (AccommodationsServiceTest.class);
  TDSSettings                 tdsSettings           = null;
  ItemBankRepository          itemBankRepository    = null;
  TesteeRepository            testeeRepository      = null;
  OpportunityRepository       opportunityRepository = null;
  @Autowired
  AccommodationsService accommodationsService;

  // Success Test Case
  @Test
  public void testgetStimulusContent () throws SQLException, ReturnStatusException {
    String testKey = null;
    boolean isGuestSession = false;
    long testeeKey = 0;
    List<Accommodations> listOfAccommodations = accommodationsService.getTestee (testKey, isGuestSession, testeeKey);
    Assert.assertTrue (listOfAccommodations != null);
    if (listOfAccommodations != null) {
      for (Accommodations acc : listOfAccommodations) {
        _logger.info ("ID::" + acc.getId ());
        _logger.info ("Label::" + acc.getLabel ());
        _logger.info ("Position::" + acc.getPosition ());
        _logger.info ("Answer Key::" + acc.getDefaults ());
        _logger.info ("Copy right Key::" + acc.getDependencies ());
        _logger.info ("Credit Key::" + acc.getTypes ());
      }
    }
  }

  // Failure Test Case
  @Test
  public void testgetStimulusContentFailure () throws SQLException, ReturnStatusException {
    String testKey = null;
    boolean isGuestSession = false;
    long testeeKey = 0;
    List<Accommodations> listOfAccommodations = accommodationsService.getTestee (testKey, isGuestSession, testeeKey);
    Assert.assertTrue (listOfAccommodations == null);
  }

  // Success Test Case
  @Test
  public void testGetApproved () throws SQLException, ReturnStatusException {
    OpportunityInstance oppInstance = null;
    String testKey = null;
    boolean isGuestSession = false;
    List<Accommodations> listOfAccommodations = accommodationsService.getApproved (oppInstance, testKey, isGuestSession);
    Assert.assertTrue (listOfAccommodations != null);
    if (listOfAccommodations != null) {
      for (Accommodations acc : listOfAccommodations) {
        _logger.info ("ID::" + acc.getId ());
        _logger.info ("Label::" + acc.getLabel ());
        _logger.info ("Position::" + acc.getPosition ());
        _logger.info ("Defaults::" + acc.getDefaults ());
        _logger.info ("Dependencies::" + acc.getDependencies ());
        _logger.info ("Types::" + acc.getTypes ());
      }
    }
  }

  // Failure Test Case
  @Test
  public void testGetApprovedFailure () throws SQLException, ReturnStatusException {
    OpportunityInstance oppInstance = null;
    String testKey = null;
    boolean isGuestSession = false;
    List<Accommodations> listOfAccommodations = accommodationsService.getApproved (oppInstance, testKey, isGuestSession);
    Assert.assertTrue (listOfAccommodations == null);
  }

  // Success Test Case
  @Test
  public void testApproveListOfOpenTestAcc () throws SQLException, ReturnStatusException {
    OpportunityInstance oppInstance = null;
    List<OpenTestAcc> oppTestAccs = null;
    accommodationsService.approveListOfOpenTestAcc (oppInstance, oppTestAccs);
  }

  // Success Test Case
  // @Test
  // public void testApproveListOfsegmentsData () throws SQLException,
  // ReturnStatusException {
  // OpportunityInstance oppInstance = null;
  // List<?> oppTestAccs = null;
  // accommodationsService.approveListOfsegmentsData (oppInstance, oppTestAccs);
  // }

  // Success Test Case
  @Test
  public void testApprove () throws SQLException, ReturnStatusException {
    OpportunityInstance oppInstance = null;
    List<String> oppTestAccs = null;
    accommodationsService.approve (oppInstance, oppTestAccs);
  }

}
