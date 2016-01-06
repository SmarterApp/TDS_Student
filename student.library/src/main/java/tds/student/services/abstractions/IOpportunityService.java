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

import TDS.Shared.Browser.BrowserInfo;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.services.data.ApprovalInfo;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunitySegment.OpportunitySegments;
import tds.student.sql.data.OpportunityStatus;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestSegment.TestSegmentApproval;
import tds.student.sql.data.TestSelection;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;

/**
 * @author temp_rreddy
 * 
 */
public interface IOpportunityService
{
  List<TestSelection> getEligibleTests (Testee testee, TestSession session, String grade, BrowserInfo browserInfo) throws ReturnStatusException;

  OpportunityInfo openTest (Testee testee, TestSession session, String testKey) throws ReturnStatusException;

  OpportunityStatus getStatus (OpportunityInstance oppInstance) throws ReturnStatusException;

  boolean setStatus (OpportunityInstance oppInstance, OpportunityStatusChange statusChange) throws ReturnStatusException;

  ApprovalInfo checkTestApproval (OpportunityInstance oppInstance) throws ReturnStatusException;

  ApprovalInfo checkSegmentApproval (OpportunityInstance oppInstance) throws ReturnStatusException;

  void denyApproval (OpportunityInstance oppInstance) throws ReturnStatusException;

  TestConfig startTest (OpportunityInstance oppInstance, String testKey, List<String> formKeys) throws ReturnStatusException;

  OpportunitySegments getSegments (OpportunityInstance oppInstance, boolean validate) throws ReturnStatusException;

  void waitForSegment (OpportunityInstance oppInstance, int segmentPosition, TestSegmentApproval segmentApproval) throws ReturnStatusException;

  void exitSegment (OpportunityInstance oppInstance, int segmentPosition) throws ReturnStatusException;
}
