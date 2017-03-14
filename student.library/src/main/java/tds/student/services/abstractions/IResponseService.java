/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.abstractions;

import java.util.UUID;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.sql.data.AdaptiveGroup;
import tds.student.sql.data.OpportunityInstance;

/**
 * @author temp_rreddy
 * 
 */
public interface IResponseService
{
  PageGroup insertItems (OpportunityInstance oppInstance, AdaptiveGroup adaptiveGroup, boolean isMsb) throws ReturnStatusException;;

  PageList getOpportunityItems (OpportunityInstance oppInstance, boolean validate) throws ReturnStatusException;;

  PageGroup getItemGroup (OpportunityInstance oppInstance, int page, String groupID, String dateCreated, boolean validate) throws ReturnStatusException;;

  boolean isTestComplete (UUID oppKey) throws ReturnStatusException;

  void removeResponse (OpportunityInstance oppInstance, int position, String itemID, String dateCreated) throws ReturnStatusException;
}
