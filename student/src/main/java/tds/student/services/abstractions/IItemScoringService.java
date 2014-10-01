/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.abstractions;

import java.util.List;
import java.util.UUID;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemrenderer.data.IITSDocument;
import tds.itemscoringengine.ItemScore;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;
import tds.student.sql.data.OpportunityInstance;

/**
 * @author temp_rreddy
 * 
 */
public interface IItemScoringService
{
  ItemScore checkScoreability (IItemResponseScorable responseScorable, IITSDocument itsDoc) throws ReturnStatusException;

  boolean updateItemScore (UUID oppKey, IItemResponseScorable response, ItemScore score) throws ReturnStatusException;

  List<ItemResponseUpdateStatus> updateResponses (OpportunityInstance oppInstance, List<ItemResponseUpdate> responsesUpdated) throws ReturnStatusException;

  ItemScore scoreItem (UUID oppKey, IItemResponseScorable responseScorable, IITSDocument itsDoc) throws ReturnStatusException;
}
