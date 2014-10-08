/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repositorysp;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.sql.data.AdaptiveGroup;
import tds.student.sql.data.IItemResponseUpdate;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.data.OpportunityItem.OpportunityItems;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Data.ReturnStatus;

/**
 * @author temp_rreddy
 * 
 */
public class ResponseRepositoryTest extends AbstractTest
{
  private static final Logger _logger            = LoggerFactory.getLogger (ResponseRepositoryTest.class);

  ResponseRepository          responseRepository = new ResponseRepository ();

  @Test
  public void testInsertItems () {
    try {
      UUID oppKey = UUID.fromString ("D2AAA0AA-E338-4A96-A336-871838F6CB00");
      UUID sessionKey = UUID.fromString ("9B6CF5E5-E202-451D-8C89-9207440A2A84");
      UUID browserKey = UUID.fromString ("9CFCD4AA-14A6-462D-96C3-D4831CC408B5");

      OpportunityInstance opportunityInstance = new OpportunityInstance (oppKey,
          sessionKey, browserKey);
      AdaptiveGroup adaptiveGroup = new AdaptiveGroup ();
      adaptiveGroup.setBankKey (1);
      adaptiveGroup.setGroupID ("G-159-59");
      adaptiveGroup.setNumItemsRequired (-1);
      adaptiveGroup.setSegmentID ("MCA-Science-HS");
      adaptiveGroup.setSegmentPosition (1);
      adaptiveGroup.setPage (1);
      OpportunityItems opportunityItemsValue = responseRepository.insertItems
          (opportunityInstance, adaptiveGroup);
//      _logger.info ("Opportunity Items Value::" + opportunityItemsValue.getItems
//          ().size ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  @Test
  public void testUpdateScoredResponse () {
    try {

      UUID oppKey = UUID.fromString ("9F1DCD39-111A-4417-8464-00A0E1291E4D");
      UUID sessionKey = UUID.fromString ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
      UUID browserKey = UUID.fromString ("99CDB138-17B3-4DFA-B892-D4E0060FD477");

      OpportunityInstance opportunityInstance = new OpportunityInstance (oppKey,
          sessionKey, browserKey);
      IItemResponseUpdate iItemResponseUpdate = null;
      ReturnStatus returnStatus = responseRepository.updateScoredResponse
          (opportunityInstance, iItemResponseUpdate, 1, null, null, 1);
      Assert.assertTrue (returnStatus != null);
      if (returnStatus != null)
        _logger.info ("Return Status Value::" + returnStatus.getStatus ());

    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  @Test
  public void testGetOpportunityItems () {
   /* try {
      UUID uuid = UUID.fromString ("F3F99ABD-B24E-4776-9DD7-045F5F579ED1");
      OpportunityItems opportunityItemsValue = responseRepository.getOpportunityItems (uuid);
      Assert.assertTrue (opportunityItemsValue != null);
      if (opportunityItemsValue != null)
        _logger.info ("Opportunity Items Value::" + opportunityItemsValue.getItems ());
      List<OpportunityItem> OpportunityItemsList = opportunityItemsValue.getItems ();
      _logger.info ("Opportunity Items List Size::" + OpportunityItemsList.size ());
      if (OpportunityItemsList.size () > 0)
        for (int i = 0; i < OpportunityItemsList.size (); i++)
        {
          _logger.info ("Opportunity Items Value Group Id::" + OpportunityItemsList.get (i).getGroupID ());
          _logger.info ("Opportunity Items Value GroupItemsRequired::" + OpportunityItemsList.get (i).getGroupItemsRequired ());
          _logger.info ("Opportunity Items Value ItemFile::" + OpportunityItemsList.get (i).getItemFile ());
          _logger.info ("Opportunity Items Value BankKey::" + OpportunityItemsList.get (i).getBankKey ());
          _logger.info ("Opportunity Items Value DateCreated::" + OpportunityItemsList.get (i).getDateCreated ());
          _logger.info ("Opportunity Items Value ItemKey::" + OpportunityItemsList.get (i).getItemKey ());
        }
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }*/
  }

  @Test
  public void testGetOpportunityItemsWithValidation () {
    /* try {
      UUID oppkey = UUID.fromString ("F3F99ABD-B24E-4776-9DD7-045F5F579ED1");
      UUID session = UUID.fromString ("502165F2-909C-4FFB-8AB1-0EB2A398C8FF");
      UUID browser = UUID.fromString ("4A10B61A-68BB-4506-9DE0-DE166CBD49E4");
      OpportunityInstance opportunityInstance = new
          OpportunityInstance (oppkey, session, browser);
      OpportunityItems opportunityItemsValue =
          responseRepository.getOpportunityItemsWithValidation (opportunityInstance);
      Assert.assertTrue (opportunityItemsValue != null);
      if (opportunityItemsValue != null)
        _logger.info ("Opportunity Items Value::" + opportunityItemsValue.getItems ());

      List<OpportunityItem> OpportunityItemsList = opportunityItemsValue.getItems ();
      _logger.info ("Opportunity Items List Size::" + OpportunityItemsList.size ());
      if (OpportunityItemsList.size () > 0)
        for (int i = 0; i < OpportunityItemsList.size (); i++)
        {
          _logger.info ("Opportunity Items Value Group Id::" + OpportunityItemsList.get (i).getGroupID ());
          _logger.info ("Opportunity Items Value GroupItemsRequired::" + OpportunityItemsList.get (i).getGroupItemsRequired ());
          _logger.info ("Opportunity Items Value ItemFile::" + OpportunityItemsList.get (i).getItemFile ());
          _logger.info ("Opportunity Items Value BankKey::" + OpportunityItemsList.get (i).getBankKey ());
          _logger.info ("Opportunity Items Value DateCreated::" + OpportunityItemsList.get (i).getDateCreated ());
          _logger.info ("Opportunity Items Value ItemKey::" + OpportunityItemsList.get (i).getItemKey ());
        }
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }*/
  }

  @Test
  public void testGetItemGroup () {
    try {
      UUID oppKey = UUID.fromString ("13D734F8-A604-47AF-BF0C-55D08E7839FA");
      UUID sessionKey = null;
      UUID browserKey = null;

      OpportunityInstance opportunityInstance = new OpportunityInstance (oppKey, sessionKey, browserKey);
      List<OpportunityItem> OpportunityItemList = responseRepository.getItemGroup (opportunityInstance, 1);
      Assert.assertTrue (OpportunityItemList != null);
      if (OpportunityItemList != null)
        _logger.info ("Opportunity Items List Size::" + OpportunityItemList.size ());
      for (int i = 0; i < OpportunityItemList.size (); i++)
      {
        _logger.info ("Opportunity Items Value Group Id::" + OpportunityItemList.get (i).getGroupID ());
        _logger.info ("Opportunity Items Value GroupItemsRequired::" + OpportunityItemList.get (i).getGroupItemsRequired ());
        _logger.info ("Opportunity Items Value ItemFile::" + OpportunityItemList.get (i).getItemFile ());
        _logger.info ("Opportunity Items Value BankKey::" + OpportunityItemList.get (i).getBankKey ());
        _logger.info ("Opportunity Items Value DateCreated::" + OpportunityItemList.get (i).getDateCreated ());
        _logger.info ("Opportunity Items Value ItemKey::" + OpportunityItemList.get (i).getItemKey ());
      }

    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  @Test
  public void testSetItemMarkForReview () {
    try {

      UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
      UUID sessionKey = UUID.fromString ("38893e8d-a5d2-4bf8-906e-3c2cbfbacc30");
      UUID browserKey = UUID.fromString ("A3161C78-314F-4337-90D4-2B0FCB50C9DF");
      Integer position = 1;
      Boolean mark = false;

      OpportunityInstance opportunityInstance = new
          OpportunityInstance (oppKey, sessionKey, browserKey);
      ReturnStatus returnStatus = responseRepository.setItemMarkForReview
          (opportunityInstance, position, mark);
      Assert.assertTrue (returnStatus != null);
      if (returnStatus != null)
        _logger.info ("Return Status Value::" + returnStatus.getStatus ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  @Test
  public void testRecordComment () {
    try {
      UUID oppKey = UUID.fromString ("F4E3EA6C-301D-448F-854C-2899A897B2B2");
      UUID sessionKey = UUID.fromString ("0A94BDC9-86E7-43B7-82FD-4CDB0AF08EC2");
      long testeeKey = -238;
      int itemposition = 4;
      String comment = "Test Comment";
      responseRepository.recordComment (oppKey, testeeKey, sessionKey, itemposition, comment);
      Assert.assertTrue (true);
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  @Test
  public void testIsTestComplete () {
    try {
      UUID uuid = UUID.fromString ("8F9972ED-C7C0-4910-A517-0E16D8D50FDE");

      boolean isTestCompleteValue = responseRepository.isTestComplete (uuid);
      _logger.info ("Test Complete Value ::" + isTestCompleteValue);
      Assert.assertTrue (isTestCompleteValue);

    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

  @Test
  public void testRemoveResponse () {
    try {

      UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
      String itemId = "159-471";
      Integer position = 1;
      String dateCreated = "2012-09-26 19:12:49.480";
      UUID sessionKey = UUID.fromString ("38893e8d-a5d2-4bf8-906e-3c2cbfbacc30");
      UUID browserKey = UUID.fromString ("A3161C78-314F-4337-90D4-2B0FCB50C9DF");

      OpportunityInstance opportunityInstance = new OpportunityInstance (oppKey, sessionKey, browserKey);
      ReturnStatus returnStatus = responseRepository.removeResponse (opportunityInstance, position, itemId, dateCreated);
      Assert.assertTrue (returnStatus != null);
      if (returnStatus != null)
        _logger.info ("Return Status Value::" + returnStatus.getStatus ());

    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }
  }

}
