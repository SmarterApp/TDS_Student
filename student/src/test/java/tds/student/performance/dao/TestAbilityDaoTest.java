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

import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import tds.dll.common.performance.utils.LegacyDbNameUtility;
import tds.dll.common.performance.utils.UuidAdapter;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.TestAbility;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@code TestOpportunityDao} implementations.
 */
public class TestAbilityDaoTest extends IntegrationTest {
    private final String clientName = "SBAC_PT";
    private final String subject = "MATH";
    private final Long testee = 9999L;
    private final Timestamp dateScored = new Timestamp(System.currentTimeMillis());
    private final Float score = 100F;
    private final Integer opportunity = 100;
    private final String testId = "SBAC-IRP-Perf-MATH-3";
    private final UUID otherOppKey = UUID.randomUUID();
    private final String adminSubject = "AdminSubject";

    @Autowired
    LegacyDbNameUtility dbNameUtility;

    @Autowired
    TestAbilityDao testAbilityDao;

    @Before
    public void setup() {
        Map<String, Object> params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(otherOppKey));
        params.put("testee", testee);
        params.put("subject", subject);
        params.put("testid", testId);
        params.put("dateScored", dateScored);
        params.put("opportunity", opportunity);
        params.put("clientName", clientName);
        params.put("adminSubject", adminSubject);

        final String TEST_OPP_SQL =
            "INSERT INTO ${sessiondb}.testopportunity (_Key, _efk_Testee, _efk_TestID, opportunity, dateScored, subject, " +
                    "clientname, _version, _efk_adminsubject, environment, isSegmented, algorithm) " +
            "VALUES (:oppKey, :testee, :testid, :opportunity, :dateScored, :subject, :clientName, 1, :adminSubject, 'dev', 0, 'fixedform')";

        namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(TEST_OPP_SQL), params);

        params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(otherOppKey));
        params.put("score", score);
        params.put("date", new Date());

        final String TEST_SCORE_SQL =
            "INSERT INTO ${sessiondb}.testopportunityscores (_fk_testopportunity, measurelabel, measureof, value, useforability, _date) " +
                    "VALUES (:oppKey, 'measure label', 'measure of', :score, 1, :date)";

        namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(TEST_SCORE_SQL), params);

        params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(otherOppKey));
        params.put("score", score);
        params.put("testee", testee);
        params.put("clientName", clientName);
        params.put("subject", subject);

        final String TESTEE_HISTORY_SQL =
                "INSERT INTO ${sessiondb}.testeehistory (_Key, _efk_Testee, clientname, Subject, initialAbility) " +
                        "VALUES (:oppKey, :testee, :clientName, :subject, :score)";

        namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(TESTEE_HISTORY_SQL), params);
    }

    @After
    public void cleanup() {
        Map<String, Object> params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(otherOppKey));
        final String DELETE_SQL1 =
                "DELETE FROM ${sessiondb}.testopportunityscores\n" +
                        "WHERE _fk_testopportunity = :oppKey";

        final String DELETE_SQL2 =
                "DELETE FROM ${sessiondb}.testopportunity\n" +
                        "WHERE _Key = :oppKey";

        final String DELETE_SQL3 =
                "DELETE FROM ${sessiondb}.testeehistory\n" +
                        "WHERE _efk_Testee = :testee";

        params.put("testee", testee);
        namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(DELETE_SQL1), params);
        namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(DELETE_SQL2), params);
        namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(DELETE_SQL3), params);
    }

    @Test
    public void getTestAbilitiesTest() {
        UUID key = UUID.randomUUID();

        List<TestAbility> testAbilities = testAbilityDao.getTestAbilities(key, clientName, subject, testee);
        assertNotNull(testAbilities);
        assertTrue(testAbilities.size() == 1);
        assertEquals(testAbilities.get(0).getOppkey(), otherOppKey);
        assertEquals(testAbilities.get(0).getOpportunity(), opportunity);
        assertEquals(testAbilities.get(0).getScore(), score);
        assertEquals(testAbilities.get(0).getTest(), testId);
    }

    @Test
    public void getMostRecentTestAbilityFromHistoryTest() {
        Float ability = testAbilityDao.getMostRecentTestAbilityFromHistory(clientName, subject, testee);
        assertEquals(ability, score);
    }

}
