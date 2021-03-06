/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

import tds.itemrenderer.data.AccLookup;
import tds.student.services.abstractions.PrintService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.OpportunityInstance;

/**
 * @author temp_rreddy
 * 
 */
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
public class PrintServiceTest extends AbstractTest
{
  @SuppressWarnings ("unused")
  private static final Logger _logger               = LoggerFactory.getLogger (PrintServiceTest.class);

  @Autowired
  @Qualifier("legacyPrintService")
  PrintService printService ;

  // Suceess Test Case
  @Test
  @Ignore("Never actually implemented")
  public void testLogin () throws SQLException, ReturnStatusException {
    OpportunityInstance oppInstance = null;
    PageGroup pageGroupToPrint = null;
    String requestParameters = null;
    boolean label = printService.printPassage (oppInstance, pageGroupToPrint, requestParameters);
    Assert.assertTrue (label);
  }

  // Failure Test Case
  @Test
  @Ignore("Never actually implemented")
  public void testLoginFailure () throws SQLException, ReturnStatusException {
    OpportunityInstance oppInstance = null;
    PageGroup pageGroupToPrint = null;
    String requestParameters = null;
    boolean label = printService.printPassage (oppInstance, pageGroupToPrint, requestParameters);
    Assert.assertFalse (label);
  }

  // Sucess Test Case
  @Test
  @Ignore("Never actually implemented")
  public void testprintItem () throws SQLException, ReturnStatusException {
    OpportunityInstance oppInstance = null;
    ItemResponse pageGroupToPrint = null;
    String requestParameters = null;
    boolean label = printService.printItem (oppInstance, pageGroupToPrint, requestParameters);
    Assert.assertTrue (label);
  }

  // Failure Test Case
  @Test
  @Ignore("Never actually implemented")
  public void testprintItemFailure () throws SQLException, ReturnStatusException {
    OpportunityInstance oppInstance = null;
    ItemResponse pageGroupToPrint = null;
    String requestParameters = null;
    boolean label = printService.printItem (oppInstance, pageGroupToPrint, requestParameters);
    Assert.assertFalse (label);
  }

  // Sucess Test Case
  @Test
  @Ignore("Never actually implemented")
  public void testPrintPassageBraille () throws SQLException, ReturnStatusException {
    TestOpportunity testOpp = null;
    PageGroup pageGroupToPrint = null;
    AccLookup accLookup = null;
    boolean label = printService.printPassageBraille (testOpp, pageGroupToPrint, accLookup);
    Assert.assertTrue (label);
  }

  // Failure Test Case
  @Test
  @Ignore("Never actually implemented")
  public void testPrintPassageBrailleFailure () throws SQLException, ReturnStatusException {
    TestOpportunity testOpp = null;
    PageGroup pageGroupToPrint = null;
    AccLookup accLookup = null;
    boolean label = printService.printPassageBraille (testOpp, pageGroupToPrint, accLookup);
    Assert.assertFalse (label);
  }

  // Sucess Test Case
  @Test
  @Ignore("Never actually implemented")
  public void tesPprintItemBraille () throws SQLException, ReturnStatusException {
    TestOpportunity testOpp = null;
    ItemResponse responseToPrint = null;
    AccLookup accLookup = null;
    boolean label = printService.printItemBraille (testOpp, responseToPrint, accLookup);
    Assert.assertTrue (label);
  }

  // Sucess Test Case
  @Test
  @Ignore("Never actually implemented")
  public void tesPrintItemBrailleFailure () throws SQLException, ReturnStatusException {
    TestOpportunity testOpp = null;
    ItemResponse responseToPrint = null;
    AccLookup accLookup = null;
    boolean label = printService.printItemBraille (testOpp, responseToPrint, accLookup);
    Assert.assertFalse (label);
  }

}
