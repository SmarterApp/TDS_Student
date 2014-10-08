/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.sql.repository.ResponseRepository;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
public class AdaptiveServiceTest extends AbstractTest
{

  @SuppressWarnings ("unused")
  private static final Logger _logger           = LoggerFactory.getLogger (AdaptiveServiceTest.class);

  ResponseRepository          responseRepositor = null;
  ResponseService             responseService   = null;

  // AdaptiveService adaptiveService = new AdaptiveService (responseService);

  // Suceess Test Case
  @Test
  public void testCreateNextItemGroup () throws SQLException, ReturnStatusException, Exception {
    // TODO mpatel Fix it once ItemRenderer is complete
    throw new Exception ();
    /*
     * OpportunityInstance oppInstance = null; int lastPage = 0; int
     * lastPosition = 0; PageGroup pageGroup =
     * adaptiveService.createNextItemGroup (oppInstance, lastPage,
     * lastPosition); Assert.assertTrue (pageGroup != null); if (pageGroup !=
     * null) { _logger.info ("File path::" + pageGroup.getFilePath ());
     * _logger.info ("Group Id::" + pageGroup.getGroupID ()); _logger.info
     * ("ID::" + pageGroup.getId ()); _logger.info ("Items Left Required::" +
     * pageGroup.getItemsLeftRequired ()); _logger.info
     * ("Items Left Unanswered::" + pageGroup.getItemsLeftUnanswered ());
     * _logger.info ("Items REquired::" + pageGroup.getItemsRequired ());
     * _logger.info ("Number::" + pageGroup.getNumber ()); _logger.info
     * ("Segment Id::" + pageGroup.getSegmentID ()); _logger.info
     * ("Segment Position::" + pageGroup.getSegmentPos ()); _logger.info
     * ("Config::" + pageGroup.getConfig ()); _logger.info ("Document::" +
     * pageGroup.getDocument ()); _logger.info ("Completed::" +
     * pageGroup.getIsCompleted ()); _logger.info ("Is Valid::" +
     * pageGroup.getIsValid ()); _logger.info ("List of item response::" +
     * pageGroup.getListOfItemResponse ()); }
     */
  }

  // Failure Test Case
  @Test
  public void testCreateNextItemGroupFailure () throws SQLException, ReturnStatusException, Exception {
    // TODO mpatel Fix it once ItemRenderer is complete
    fail ();
    /*
     * try { OpportunityInstance oppInstance = null; int lastPage = 0; int
     * lastPosition = 0; PageGroup pageGroup =
     * adaptiveService.createNextItemGroup (oppInstance, lastPage,
     * lastPosition); Assert.assertTrue (pageGroup == null); } catch
     * (ReturnStatusException exp) { ReturnStatus returnStatus =
     * exp.getReturnStatus (); _logger.error ("Status: " +
     * returnStatus.getStatus ()); _logger.error ("Reason: " +
     * returnStatus.getReason ()); _logger.error (exp.getMessage ()); } catch
     * (Exception exp) { exp.printStackTrace (); _logger.error (exp.getMessage
     * ()); }
     */
  }

}
