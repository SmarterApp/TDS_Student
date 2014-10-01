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

import tds.student.sql.data.BrowserCapabilities;

import tds.student.sql.data.ClientLatency;

import tds.student.sql.data.OpportunityAccommodation;

import tds.student.sql.data.OpportunityInfo;

import tds.student.sql.data.OpportunityInstance;

import tds.student.sql.data.OpportunitySegment.OpportunitySegments;

import tds.student.sql.data.OpportunityStatus;

import tds.student.sql.data.ServerLatency;

import tds.student.sql.data.TestConfig;

import tds.student.sql.data.TestSelection;

import tds.student.sql.data.ToolUsed;

/**
 * 
 * @author temp_rreddy
 * 
 * 
 */

public interface IOpportunityRepository

{

  List<TestSelection> getEligibleTests (long testeeKey, UUID sessionKey, String grade) throws ReturnStatusException;

  ReturnStatus approveAccommodations (OpportunityInstance oppInstance, int segment, String segmentAccoms) throws ReturnStatusException;

  OpportunityInfo openTestOpportunity (long testeeKey, String testKey, UUID sessionKey, UUID browserKey) throws ReturnStatusException;

  List<OpportunityAccommodation> getOpportunityAccommodations (OpportunityInstance oppInstance, String testKey) throws ReturnStatusException;

  TestConfig startTestOpportunity (OpportunityInstance oppInstance, String testKey, String formKeyList) throws ReturnStatusException;

  OpportunitySegments getOpportunitySegments (OpportunityInstance oppInstance) throws ReturnStatusException;

  OpportunitySegments getOpportunitySegments (UUID oppKey) throws ReturnStatusException;

  OpportunityStatus validateAccess (OpportunityInstance oppInstance) throws ReturnStatusException;

  ReturnStatus setStatus (UUID oppKey, String status, String comment) throws ReturnStatusException;

  ReturnStatus setStatusWithValidation (OpportunityInstance oppInstance, String status, String comment) throws ReturnStatusException;

  void logOpportunityClient (OpportunityInstance oppInstance, int restart, BrowserCapabilities browserCapabilities) throws ReturnStatusException;

  int recordServerLatency (OpportunityInstance oppInstance, ServerLatency serverLatency) throws ReturnStatusException;

  int recordClientLatency (OpportunityInstance oppInstance, ClientLatency clientLatency) throws ReturnStatusException;

  int recordClientLatencies (OpportunityInstance oppInstance, List<ClientLatency> clientLatencies) throws ReturnStatusException;

  int recordToolsUsed (UUID oppKey, List<ToolUsed> toolsUsed) throws ReturnStatusException;

  int submitRequest (OpportunityInstance oppInstance, int itemPage, int itemPosition, String requestType, String requestDescription, String requestValue, String requestParameters)
      throws ReturnStatusException;;

  ReturnStatus waitForSegment (OpportunityInstance oppInstance, int segment, boolean entry, boolean exit) throws ReturnStatusException;;

  ReturnStatus exitSegment (OpportunityInstance oppInstance, int segment) throws ReturnStatusException;;

  void recordComment (UUID sessionKey, long testeeKey, UUID oppKey, String comment) throws ReturnStatusException;;

  String getComment (UUID oppKey) throws ReturnStatusException;

  int getOpportunityNumber (UUID oppKey) throws ReturnStatusException;

}
