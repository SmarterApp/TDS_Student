/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.abstractions;

import java.util.ArrayList;
import java.util.List;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.student.services.data.OpenTestAcc;
import tds.student.sql.data.Accommodations;
import tds.student.sql.data.OpportunityInstance;

/**
 * @author temp_rreddy
 * 
 */
public interface IAccommodationsService
{
  // / <summary>
  // / Get the test and segments accommodations for PT selection.
  // / </summary>
  List<Accommodations> getTestee (String testKey, boolean isGuestSession, long testeeKey) throws ReturnStatusException;

  // Shiva: the following API existed in .NET. In java the compiler complaines
  // that "the two methods have the same erasure".
  // In .NET this one is only called within AccommodationService. So no need to
  // have a public method.
  /*
   * void approve (OpportunityInstance oppInstance, List<OpenTestAcc>
   * oppTestAccs) throws ReturnStatusException;
   */

  void approve (OpportunityInstance oppInstance, List<String> segmentsData) throws ReturnStatusException;

  // / <summary>
  // / This function gets all the accommodations for a approved opportunity.
  // / </summary>
  List<Accommodations> getApproved (OpportunityInstance oppInstance, String testKey, boolean isGuestSession) throws ReturnStatusException;
}
