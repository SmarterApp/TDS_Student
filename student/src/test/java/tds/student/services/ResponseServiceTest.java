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
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.sql.data.AdaptiveGroup;
import tds.student.sql.data.OpportunityInstance;
//import tds.student.sql.data.TestConfig.TestConfigUpdate;
import tds.student.sql.repository.ResponseRepository;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
public class ResponseServiceTest extends AbstractTest
{
  private static final Logger _logger            = LoggerFactory.getLogger (ResponseServiceTest.class);
  ResponseRepository          responseRepository = null;
  ResponseService             responseService    = null;

  // Suceess Test Case
  ////@Test
  public void testLogin () throws SQLException, ReturnStatusException {
    try {
      OpportunityInstance oppInstance = null;
      AdaptiveGroup adaptiveGroup = null;
      PageGroup pageGroup = responseService.insertItems (oppInstance, adaptiveGroup, false);
      Assert.assertTrue (pageGroup != null);
      if (pageGroup != null) {
        _logger.info ("File path::" + pageGroup.getFilePath ());
        _logger.info ("Group Id::" + pageGroup.getGroupID ());
        _logger.info ("ID::" + pageGroup.getId ());
        _logger.info ("Items Left Required::" + pageGroup.getItemsLeftRequired ());
        _logger.info ("Items Left Unanswered::" + pageGroup.getItemsLeftUnanswered ());
        _logger.info ("Items REquired::" + pageGroup.getItemsRequired ());
        _logger.info ("Number::" + pageGroup.getNumber ());
        _logger.info ("Segment Id::" + pageGroup.getSegmentID ());
        _logger.info ("Segment Position::" + pageGroup.getSegmentPos ());
//        _logger.info ("Config::" + pageGroup.getConfig ());
        _logger.info ("Document::" + pageGroup.getDocument ());
        _logger.info ("Completed::" + pageGroup.getIsCompleted ());
        _logger.info ("Is Valid::" + pageGroup.getIsValid ());
//        _logger.info ("List of item response::" + pageGroup.getListOfItemResponse ());
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
  //@Test
  public void testLoginFailure () throws SQLException, ReturnStatusException {
    try {
      OpportunityInstance oppInstance = null;
      AdaptiveGroup adaptiveGroup = null;
      PageGroup pageGroup = responseService.insertItems (oppInstance, adaptiveGroup, false);
      Assert.assertTrue (pageGroup == null);
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

  // Sucess Test Case
  //@Test
  public void testOpportunityItems () throws SQLException, ReturnStatusException {
    /*try {
      OpportunityInstance oppInstance = null;
      boolean adaptiveGroup = false;
      Map.Entry<PageList, TestConfigUpdate> pageGroup = responseService.getOpportunityItems (oppInstance, adaptiveGroup);
      Assert.assertTrue (pageGroup != null);
      if (pageGroup != null) {
        _logger.info ("Key ::" + pageGroup.getKey ());
        _logger.info ("Value::" + pageGroup.getValue ());
      }
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }*/
  }

  // Failure Test Case
  //@Test
  public void testOpportunityItemsFAilure () throws SQLException, ReturnStatusException {
    /*try {
      OpportunityInstance oppInstance = null;
      boolean adaptiveGroup = false;
      Map.Entry<PageList, TestConfigUpdate> pageGroup = responseService.getOpportunityItems (oppInstance, adaptiveGroup);
      Assert.assertTrue (pageGroup == null);
    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }*/
  }

  // Sucess Test Case
  //@Test
  public void testGetItemGroup () throws SQLException, ReturnStatusException {
    try {
      OpportunityInstance oppInstance = null;
      int page = 0;
      String groupID = null;
      String dateCreated = null;
      boolean validate = false;
      PageGroup pageGroup = responseService.getItemGroup (oppInstance, page, groupID, dateCreated, validate);
      Assert.assertTrue (pageGroup != null);
      if (pageGroup != null) {
        _logger.info ("File path::" + pageGroup.getFilePath ());
        _logger.info ("Group Id::" + pageGroup.getGroupID ());
        _logger.info ("ID::" + pageGroup.getId ());
        _logger.info ("Items Left Required::" + pageGroup.getItemsLeftRequired ());
        _logger.info ("Items Left Unanswered::" + pageGroup.getItemsLeftUnanswered ());
        _logger.info ("Items REquired::" + pageGroup.getItemsRequired ());
        _logger.info ("Number::" + pageGroup.getNumber ());
        _logger.info ("Segment Id::" + pageGroup.getSegmentID ());
        _logger.info ("Segment Position::" + pageGroup.getSegmentPos ());
//        _logger.info ("Config::" + pageGroup.getConfig ());
        _logger.info ("Document::" + pageGroup.getDocument ());
        _logger.info ("Completed::" + pageGroup.getIsCompleted ());
        _logger.info ("Is Valid::" + pageGroup.getIsValid ());
//        _logger.info ("List of item response::" + pageGroup.getListOfItemResponse ());
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
  //@Test
  public void testGetItemGroupFailure () throws SQLException, ReturnStatusException {
    try {
      OpportunityInstance oppInstance = null;
      int page = 0;
      String groupID = null;
      String dateCreated = null;
      boolean validate = false;
      PageGroup pageGroup = responseService.getItemGroup (oppInstance, page, groupID, dateCreated, validate);
      Assert.assertTrue (pageGroup == null);

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

  // Sucess Test Case
  //@Test
  public void testisTestComplete () throws SQLException, ReturnStatusException {
    try {
      UUID uuid = null;
      boolean pageGroup = responseService.isTestComplete (uuid);
      Assert.assertTrue (pageGroup);
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
  //@Test
  public void testisTestCompleteFailure () throws SQLException, ReturnStatusException {
    try {
      UUID uuid = null;
      boolean pageGroup = responseService.isTestComplete (uuid);
      Assert.assertFalse (pageGroup);
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

  // Sucess Test Case
  //@Test
  public void testremoveResponse () throws SQLException, ReturnStatusException {
    try {
      OpportunityInstance oppInstance = null;
      int position = 0;
      String itemID = null;
      String dateCreated = null;
      responseService.removeResponse (oppInstance, position, itemID, dateCreated);
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
