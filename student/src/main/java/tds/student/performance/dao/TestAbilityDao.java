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

import org.springframework.stereotype.Repository;
import tds.student.performance.domain.TestAbility;

import java.util.List;
import java.util.UUID;

/**
 * Created by emunoz on 12/28/15.
 */
public interface TestAbilityDao {
    /**
     * Gets the list of {@link TestAbility} objects from the testopportunity and testopportunityscores tables
     *
     * @param oppKey
     * @param clientname
     * @param subject
     * @param testee
     * @return
     */
    List<TestAbility> getTestAbilities(UUID oppKey, String clientname, String subject, Long testee);

    /**
     * Gets the most recent initialAbility from the testeehistory table
     * @param clientname
     * @param subject
     * @param testee
     * @return
     */
    Float getMostRecentTestAbilityFromHistory(String clientname, String subject, Long testee);
}
