package tds.student.performance.dao;

import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestOpportunityAudit;
import tds.student.performance.utils.DateUtility;
import tds.student.performance.utils.UuidAdapter;

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
    DateUtility dateUtility;

    private final UUID expectedOpportunityKey = UUID.randomUUID();
    private final UUID expectedSessionKey = UUID.randomUUID();
    private final UUID expectedBrowserKey = UUID.randomUUID();
    private final String expectedTestKey = "(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016";
    private final Long expectedTestee = Math.round(ThreadLocalRandom.current().nextDouble(100, 500));
    private final String expectedTestId = "SBAC-IRP-Perf-MATH-3";
    private final Integer expectedOpportunity = 1;
    private final String expectedStatus = "pending";
    private final String expectedSubject = "MATH";
    private final String expectedClientName = "SBAC_PT";
    private final Boolean expectedIsSegmented = false;
    private final String expectedAlgorithm = "fixedform";
    private final String expectedEnvironment = "dev";
    private final Long expectedSimulationSegmentCount = 0L;
    private final Integer version = 1;

    @Before
    public void setup() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(expectedOpportunityKey));
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(expectedSessionKey));
        parameters.put("browserKey", UuidAdapter.getBytesFromUUID(expectedBrowserKey));
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
                "INSERT INTO session.testopportunity(_key, _fk_session, _fk_browser, _efk_adminsubject, _efk_testee, _efk_testid, subject, clientname, issegmented, algorithm, _version, environment)" +
                "VALUES(:key, :sessionKey, :browserKey, :testKey, :testee, :testId, :subject, :clientName, :isSegmented, :algorithm, :version, :environment)";

        namedParameterJdbcTemplate.update(SQL, parameters);
    }

    @After
    public void teardown() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", expectedOpportunityKey);

        final String SQL = "DELETE FROM session.testopportunity WHERE _key = :key";

        namedParameterJdbcTemplate.update(SQL, parameters);
    }

    @Test
    public void should_Get_a_TestOpportunity() {
        TestOpportunity result = testOpportunityDao.get(expectedOpportunityKey);

        Assert.assertNotNull(result);
        Assert.assertEquals(expectedOpportunityKey, result.getKey());
        Assert.assertEquals(expectedSessionKey, result.getSessionKey());
        Assert.assertEquals(expectedBrowserKey, result.getBrowserKey());
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
        UUID key = UUID.randomUUID();

        TestOpportunity result = testOpportunityDao.get(key);

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
                "INSERT INTO session.testopportunity(_key, _fk_session, status, _efk_testee, _efk_testid, clientname, _version, _efk_adminsubject, environment, issegmented, algorithm) " +
                "VALUES (:key, :sessionKey, :status, :testee, 'SBAC-IRP-Perf-MATH-3', 'SBAC_PT', 1234, 'MATH', 'dev', 0, 'fixedform')";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(sessionKey));
        parameters.put("status", "pending");
        parameters.put("testee", "1111");
        parameters.put("key", UuidAdapter.getBytesFromUUID(UUID.randomUUID()));
        namedParameterJdbcTemplate.update(SQL, parameters);

        parameters.put("status", "review");
        parameters.put("testee", "2222");
        parameters.put("key", UuidAdapter.getBytesFromUUID(UUID.randomUUID()));
        namedParameterJdbcTemplate.update(SQL, parameters);

        List<TestOpportunity> results = testOpportunityDao.getBySessionAndStatus(sessionKey, "Opportunity", "inuse");

        Assert.assertEquals(2, results.size());
    }

    /**
     * Currently, the assert on this test fails because the opportunityKey and sessionKey values do not have legit
     * statuses, therefore the INSERT... SELECT statement in TestOpportunityAuditDao.create doesn't insert any records.
     * The SQL in TestOpportunityAuditDao.create is valid, but still need to come up with an accurate unit test.
     */
    @Ignore
    @Test
    public void should_Create_TestOpportunityAudit_Records_For_Specified_TestOpportunity() {
        Timestamp dateAccessed = dateUtility.getTimestamp();

        TestOpportunityAudit auditRecord = new TestOpportunityAudit(
                expectedOpportunityKey,
                dateAccessed,
                "unittest",
                expectedSessionKey,
                "unittest host",
                "unittest_db"
        );

        testOpportunityDao.createAudit(auditRecord);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(expectedOpportunityKey));
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(expectedSessionKey));
        parameters.put("dateAccessed", dateAccessed);

        final String SQL = "SELECT COUNT(*) AS count FROM archive.opportunityaudit WHERE _fk_testopportunity = :key AND _fk_session = :sessionKey AND dateaccessed = :dateAccessed";
        final Integer result = namedParameterJdbcTemplate.queryForInt(SQL, parameters);

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
