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
import tds.dll.common.performance.utils.DateUtility;
import tds.dll.common.performance.utils.LegacyDbNameUtility;
import tds.dll.common.performance.utils.UuidAdapter;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestOpportunityAudit;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tests for {@code TestOpportunityDao} implementations.
 */

public class TestOpportunityDaoTest extends IntegrationTest {
    @Autowired
    TestOpportunityDao testOpportunityDao;

    @Autowired
    LegacyDbNameUtility dbNameUtility;

    @Autowired
    DateUtility dateUtility;

    private final String expectedTestKey = "(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016";
    private final Long expectedTestee = Math.round(ThreadLocalRandom.current().nextDouble(100, 500));
    private final String expectedTestId = "MY-SUPER-AWESOME-TEST-ID";
    private final Integer expectedOpportunity = 1;
    private final String expectedStatus = "pending";
    private final String expectedSubject = "MATH";
    private final String expectedClientName = "SBAC_PT";
    private final Boolean expectedIsSegmented = false;
    private final String expectedAlgorithm = "fixedform";
    private final String expectedEnvironment = "Development";
    private final Long expectedSimulationSegmentCount = 0L;
    private final Integer version = 1;

//    @Before
//    public void setup() {
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("key", UuidAdapter.getBytesFromUUID(expectedOpportunityKey));
//        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(expectedSessionKey));
//        parameters.put("browserKey", UuidAdapter.getBytesFromUUID(expectedBrowserKey));
//        parameters.put("testKey", expectedTestKey);
//        parameters.put("testee", expectedTestee);
//        parameters.put("testId", expectedTestId);
//        parameters.put("subject", expectedSubject);
//        parameters.put("clientName", expectedClientName);
//        parameters.put("isSegmented", expectedIsSegmented);
//        parameters.put("algorithm", expectedAlgorithm);
//        parameters.put("version", version);
//        parameters.put("environment", expectedEnvironment);
//
//        final String SQL =
//                "INSERT INTO session.testopportunity(_key, _fk_session, _fk_browser, _efk_adminsubject, _efk_testee, _efk_testid, subject, clientname, issegmented, algorithm, _version, environment)" +
//                "VALUES(:key, :sessionKey, :browserKey, :testKey, :testee, :testId, :subject, :clientName, :isSegmented, :algorithm, :version, :environment)";
//
//        namedParameterJdbcTemplate.update(SQL, parameters);
//    }
//
//    @After
//    public void teardown() {
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("key", expectedOpportunityKey);
//
//        final String SQL = "DELETE FROM session.testopportunity WHERE _key = :key";
//
//        namedParameterJdbcTemplate.update(SQL, parameters);
//    }

    private void seedOpportunity(UUID oppKey, UUID sessionKey, UUID browserKey) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(oppKey));
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(sessionKey));
        parameters.put("browserKey", UuidAdapter.getBytesFromUUID(browserKey));
        parameters.put("testKey", expectedTestKey);
        parameters.put("testee", expectedTestee);
        parameters.put("testId", expectedTestId);
        parameters.put("subject", expectedSubject);
        parameters.put("clientName", expectedClientName);
        parameters.put("isSegmented", expectedIsSegmented);
        parameters.put("algorithm", expectedAlgorithm);
        parameters.put("version", version);
        parameters.put("environment", expectedEnvironment);

        final String SQL =
                "INSERT INTO ${sessiondb}.testopportunity(_key, _fk_session, _fk_browser, _efk_adminsubject, _efk_testee, _efk_testid, subject, clientname, issegmented, algorithm, _version, environment)" +
                        "VALUES(:key, :sessionKey, :browserKey, :testKey, :testee, :testId, :subject, :clientName, :isSegmented, :algorithm, :version, :environment)";

        namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(SQL), parameters);
    }

    @Test
    public void should_Get_a_TestOpportunity() {
        UUID oppKey = UUID.randomUUID();
        UUID sessionKey = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        seedOpportunity(oppKey, sessionKey, browserKey);

        TestOpportunity result = testOpportunityDao.get(oppKey);

        Assert.assertNotNull(result);
        Assert.assertEquals(oppKey, result.getKey());
        Assert.assertEquals(sessionKey, result.getSessionKey());
        Assert.assertEquals(browserKey, result.getBrowserKey());
        Assert.assertEquals(expectedTestKey, result.getTestKey());
        Assert.assertEquals(expectedTestee, result.getTestee());
        Assert.assertEquals(expectedTestId, result.getTestId());
        Assert.assertEquals(expectedOpportunity, result.getOpportunity());
        Assert.assertEquals(expectedStatus, result.getStatus());
        Assert.assertEquals(expectedSubject, result.getSubject());
        Assert.assertEquals(expectedClientName, result.getClientName());
        Assert.assertEquals(expectedIsSegmented, result.getIsSegmented());
        Assert.assertEquals(expectedAlgorithm, result.getAlgorithm());
        Assert.assertEquals(expectedEnvironment, result.getEnvironment());
        Assert.assertEquals(expectedSimulationSegmentCount, result.getSimulationSegmentCount());
    }

    @Test
    public void should_Return_Null_When_a_TestOpportunity_is_Not_Found() {
        TestOpportunity result = testOpportunityDao.get(UUID.randomUUID());

        Assert.assertNull(result);
    }

    @Test
    public void should_Return_Empty_List_When_Invalid_Data() {
        List<TestOpportunity> results = testOpportunityDao.getBySessionAndStatus(UUID.randomUUID(), "Opportunity", "inuse");

        Assert.assertEquals(0, results.size());
    }

    @Test
    public void should_Return_2_Records_For_Specific_Session() {
        // insert the records we want to test
        UUID sessionKey = UUID.randomUUID();

        final String SQL =
                "INSERT INTO ${sessiondb}.testopportunity(_key, _fk_session, status, _efk_testee, _efk_testid, clientname, _version, _efk_adminsubject, environment, issegmented, algorithm) " +
                "VALUES (:key, :sessionKey, :status, :testee, 'SBAC-IRP-Perf-MATH-3', 'SBAC_PT', 1234, 'MATH', 'dev', 0, 'fixedform')";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(sessionKey));
        parameters.put("status", "pending");
        parameters.put("testee", "1111");
        parameters.put("key", UuidAdapter.getBytesFromUUID(UUID.randomUUID()));
        namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(SQL), parameters);

        parameters.put("status", "review");
        parameters.put("testee", "2222");
        parameters.put("key", UuidAdapter.getBytesFromUUID(UUID.randomUUID()));
        namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(SQL), parameters);

        List<TestOpportunity> results = testOpportunityDao.getBySessionAndStatus(sessionKey, "Opportunity", "inuse");

        Assert.assertEquals(2, results.size());
    }

    @Test
    public void should_Create_TestOpportunityAudit_Records_For_Specified_TestOpportunity() {
        UUID oppKey = UUID.randomUUID();
        UUID sessionKey = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        seedOpportunity(oppKey, sessionKey, browserKey);

        Timestamp dateAccessed = dateUtility.getTimestamp();

        TestOpportunityAudit auditRecord = new TestOpportunityAudit(
                oppKey,
                dateAccessed,
                "unittest",
                sessionKey,
                "unittest host",
                "unittest_db"
        );

        testOpportunityDao.createAudit(auditRecord);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(oppKey));
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(sessionKey));
        parameters.put("dateAccessed", dateAccessed);

        final String SQL = "SELECT COUNT(*) AS count FROM ${archivedb}.opportunityaudit WHERE _fk_testopportunity = :key AND _fk_session = :sessionKey AND dateaccessed = :dateAccessed";
        final Integer result = namedParameterJdbcTemplate.queryForObject(dbNameUtility.setDatabaseNames(SQL), parameters, Integer.class);

        Assert.assertEquals((Integer)1, result);
    }

    // TODO:  refactor test so that it doesn't depend on a hard-coded UUID/restartCount combo.
   @Ignore
    @Test
    public void should_Get_a_Resume_Item_Position() {
        UUID opportunityKey = UUID.fromString("7cdeb5fe-9afd-4b60-b2fb-2c69cd8b46b5");
        Integer restartCount = 37;

        Integer result = testOpportunityDao.getResumeItemPosition(opportunityKey, restartCount);

        Assert.assertNotNull(result);
    }
}
