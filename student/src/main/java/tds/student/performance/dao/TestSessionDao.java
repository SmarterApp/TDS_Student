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
package tds.student.performance.dao;

import tds.student.performance.domain.SessionAudit;
import tds.student.performance.domain.TestSession;
import tds.student.performance.domain.TestSessionTimeLimitConfiguration;

import java.util.List;
import java.util.UUID;

/**
 * Created by jjohnson on 12/24/15.
 */
public interface TestSessionDao {
    TestSession get(UUID key);
    TestSessionTimeLimitConfiguration getTimeLimitConfiguration(String clientName);
    TestSessionTimeLimitConfiguration getTimeLimitConfiguration(String clientName, String testId);
    void pause(TestSession session, String reason);
    String validateProctorSession(TestSession testSession);
    String validateProctorSession(TestSession testSession, Long proctorKey, UUID browserKey);
    void createAudit(SessionAudit sessionAudit);
}
