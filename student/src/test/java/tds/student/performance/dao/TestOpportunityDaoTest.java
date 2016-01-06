package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.exceptions.ExceptionIncludingMockitoWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import tds.dll.api.ICommonDLL;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.services.LegacyErrorHandlerService;
import tds.student.performance.services.LegacyTestOpportunityService;
import tds.student.performance.utils.LegacySqlConnection;
import tds.student.performance.utils.UuidAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tests for {@code TestOpportunityDao} implementations.
 */

public class TestOpportunityDaoTest extends IntegrationTest {
    @Autowired
    TestOpportunityDao testOpportunityDao;

    /**
     * Record used for testing:
     * # key, sessionKey, browserKey, testKey, testee, testId, test, opportunity, status, dateStarted, dateChanged, rcnt, gpRestarts, testLength, subject, clientName
     '9F8817580B4A4EAAB59FB6DEA0934223', '2B20031D4BD842A89963F6FFA44A9271', 'A27DAB06891648B3AB996E95EC7AE3BF', '(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016', '168', 'SBAC-IRP-Perf-MATH-3', '(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016', '1', 'started', '2015-12-23 23:23:13.028', '2015-12-23 23:30:15.409', '1', '1', '4', 'MATH', 'SBAC_PT'
     */
    // TODO: this record doesn't appear to be here for me at least
    @Test
    public void should_Get_a_TestOpportunity() {
        UUID key = UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934223");
        UUID expectedSessionKey = UUID.fromString("50FB18AD-602D-44F6-897C-68AC90037FA5");
        UUID expectedBrowserKey = UUID.fromString("0FBB9BCF-80C2-4D95-B425-17AF9A8A4B1E");

        TestOpportunity result = testOpportunityDao.get(key);

        Assert.assertNotNull(result);
        Assert.assertEquals(key, result.getKey());
        Assert.assertEquals(expectedSessionKey, result.getSessionKey());
        Assert.assertEquals(expectedBrowserKey, result.getBrowserKey());
        Assert.assertEquals("(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016", result.getTestKey());
        Assert.assertEquals((Double)168d, result.getTestee());
        Assert.assertEquals("SBAC-IRP-Perf-MATH-3", result.getTestId());
        Assert.assertEquals((Integer)1, result.getOpportunity());
        Assert.assertEquals("paused", result.getStatus());
        Assert.assertEquals("MATH", result.getSubject());
        Assert.assertEquals("SBAC_PT", result.getClientName());
        Assert.assertEquals(false, result.getIsSegmented());
        Assert.assertEquals("fixedform", result.getAlgorithm());
        Assert.assertEquals("dev", result.getEnvironment());
        Assert.assertEquals((Integer)0, result.getSimulationSegmentCount());
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

        final String SQL = "INSERT INTO session.testopportunity " +
                "(_key, _fk_session, status, _efk_testee, _efk_testid, clientname, _version, _efk_adminsubject, environment, issegmented, algorithm) " +
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
}
