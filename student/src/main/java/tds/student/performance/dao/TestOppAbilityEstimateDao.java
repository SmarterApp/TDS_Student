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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.student.performance.domain.TestOppAbilityEstimate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by emunoz on 1/4/16.
 */
public interface TestOppAbilityEstimateDao {
    /**
     * Creates and inserts a {@link TestOppAbilityEstimate} record into the session.testoppabilityestimate
     * table.
     *
     * @param estimate the object representing a row in the testoppabilityestimate table
     */
    void create(TestOppAbilityEstimate estimate);


    /**
     * Creates and inserts a record into the session.testoppabilityestimate by obtaining the
     * strand value from the itembankdb and testopportunity tables.
     *
     * @param oppKey
     * @param ability
     * @param date
     */
    void createFromItemBankAndTestOpp(UUID oppKey, Float ability, Timestamp date);
}
