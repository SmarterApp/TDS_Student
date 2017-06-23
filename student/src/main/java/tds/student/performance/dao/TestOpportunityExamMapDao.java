/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.student.performance.dao;

import java.util.UUID;

/**
 * Used to store mapping from legacy test opportunity IDs to the new exam IDs.  This is used when in the mode of calling
 * both the legacy services as well as the new services.  Eventually this will be removed.
 */
public interface TestOpportunityExamMapDao {

    /**
     * Insert a record mapping the test opportunity ID and exam ID
     * @param testOpportunityId test opportunity ID from session.testopportunity table
     * @param examId exam ID used in the new exam.exam table
     */
    void insert(final UUID testOpportunityId, final UUID examId);
}
