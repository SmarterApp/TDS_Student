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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.services.abstractions.IItemScoringService;
import tds.student.sql.repository.ItemBankRepository;
import tds.student.sql.repository.ScoringRepository;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
public class TimerScoringServiceTest extends AbstractTest
{

  @SuppressWarnings ("unused")
  private static final Logger _logger                 = LoggerFactory.getLogger (TimerScoringServiceTest.class);

  // TODO: Need to configure a Spring-aware test runner and Spring context;
  ScoringRepository           scoringRepository       = new ScoringRepository ();
  IItemScoringService         itemScoringService      = null;
  TestScoringService          testScoringService      = null;
  ItemBankRepository          IItemBankRepository     = null;
  ContentService              contentService          = null;
  TimerScoringService         timerScoringServiceTest = null;

  // Suceess Test Case
  @Test
  public void testProcessUnscoredItems () throws SQLException, ReturnStatusException {
    timerScoringServiceTest.processUnscoredItems ();
  }
}
