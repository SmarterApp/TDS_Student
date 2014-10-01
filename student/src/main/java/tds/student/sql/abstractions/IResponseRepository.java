/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.abstractions;

import java.util.List;
import java.util.UUID;

import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

import tds.student.sql.data.AdaptiveGroup;

import tds.student.sql.data.IItemResponseUpdate;

import tds.student.sql.data.OpportunityInstance;

import tds.student.sql.data.OpportunityItem;

import tds.student.sql.data.OpportunityItem.OpportunityItems;

/**
 * 
 * @author temp_rreddy
 * 
 * 
 */

public interface IResponseRepository

{

  OpportunityItems insertItems (OpportunityInstance oppInstance, AdaptiveGroup adaptiveGroup) throws ReturnStatusException;

  ReturnStatus updateScoredResponse (OpportunityInstance oppInstance, IItemResponseUpdate responseUpdate, int score, String scoreStatus, String scoreRationale, long scoreLatency) throws ReturnStatusException;

  OpportunityItems getOpportunityItems (UUID oppKey) throws ReturnStatusException;

  OpportunityItems getOpportunityItemsWithValidation (OpportunityInstance oppInstance) throws ReturnStatusException;

  OpportunityItems getItemGroup (OpportunityInstance oppInstance, int pageNumber, String groupID, String dateCreated, boolean validateAccess) throws ReturnStatusException;

  ReturnStatus setItemMarkForReview (OpportunityInstance oppInstance, int position, boolean mark) throws ReturnStatusException;

  void recordComment (UUID sessionKey, long testeeKey, UUID oppKey, int position, String comment) throws ReturnStatusException;

  boolean isTestComplete (UUID oppKey) throws ReturnStatusException;

  ReturnStatus removeResponse (OpportunityInstance oppInstance, int position, String itemID, String dateCreated) throws ReturnStatusException;

}
