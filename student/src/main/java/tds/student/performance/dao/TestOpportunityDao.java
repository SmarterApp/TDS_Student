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

import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestOpportunityAudit;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Created by jjohnson on 12/24/15.
 */
public interface TestOpportunityDao {
    TestOpportunity get(UUID key);
    List<TestOpportunity> getBySessionAndStatus(UUID sessionKey, String statusUsage, String statusStage);
    void update(TestOpportunity opportunity);
    Timestamp getLastActivity(UUID key);
    void createAudit(TestOpportunityAudit testOpportunityAudit);
    Integer getResumeItemPosition(UUID opportunityKey, Integer newRestartInstance);
}