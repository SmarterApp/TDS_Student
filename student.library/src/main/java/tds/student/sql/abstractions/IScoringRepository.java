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
import java.util.Map;

import java.util.UUID;

import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

import tds.student.sql.data.IItemResponseScorable;

import tds.student.sql.data.ItemDisplayScores;

import tds.student.sql.data.OpportunityInstance;

import tds.student.sql.data.TestDisplayScores;

import tds.student.sql.data.TestScoreInput;

/**
 * 
 * @author temp_rreddy
 * 
 * 
 */

public interface IScoringRepository

{

  ReturnStatus updateItemScore (UUID oppKey, IItemResponseScorable responseScorable, int score, String scoreStatus, String scoreRationale, String scoreDimensions) throws ReturnStatusException;

  String getTestforCompleteness (UUID oppKey) throws ReturnStatusException;

  int validateCompleteness (UUID oppKey, String scoreString) throws ReturnStatusException;

  TestScoreInput getTestForScoring (UUID oppKey) throws ReturnStatusException;

  ReturnStatus insertTestScores (UUID oppKey, String scoreString) throws ReturnStatusException;

  ReturnStatus submitQAReport (UUID oppKey) throws ReturnStatusException;

  TestDisplayScores getDisplayScores (UUID oppKey) throws ReturnStatusException;

  ItemDisplayScores getResponseRationales (OpportunityInstance oppInstance) throws ReturnStatusException;
//TODO converted Tuple to Map.Entry 
  List<Map.Entry<UUID, IItemResponseScorable>> getScoreItems (int pendingMinutes, int minAttempts, int maxAttempts, int sessionType) throws ReturnStatusException;

}
