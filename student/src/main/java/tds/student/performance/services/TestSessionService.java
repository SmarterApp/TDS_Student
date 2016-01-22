/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
package tds.student.performance.services;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.performance.domain.SessionAudit;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSession;
import tds.student.performance.domain.TestSessionTimeLimitConfiguration;
import tds.student.performance.exceptions.ReturnErrorException;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by jjohnson on 12/26/15.
 */
public interface TestSessionService {
    TestSession get(UUID key);
    void pause(TestOpportunity testOpportunity, TestSession testSession) throws SQLException, ReturnStatusException, ReturnErrorException;
    void pause(TestOpportunity testOpportunity, TestSession testSession, String reason) throws SQLException, ReturnStatusException, ReturnErrorException;
    Integer getCheckInTimeLimit(String clientName);
    TestSessionTimeLimitConfiguration getTimelimitConfiguration(String clientName, String testId);
    void createAudit(SessionAudit sessionAudit);
}
