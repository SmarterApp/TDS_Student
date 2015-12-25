package tds.student.performance.dao;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import tds.student.performance.domain.TestOpportunity;

import java.util.UUID;

/**
 * Tests for {@code TestOpportunityDao} implementations.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/performance-integration-context.xml")
@TransactionConfiguration
public class TestOpportunityDaoTest {
    @Autowired
    TestOpportunityDao testOpportunityDao;

    /**
     * Record used for testing:
     * # key, sessionKey, browserKey, testKey, testee, testId, test, opportunity, status, dateStarted, dateChanged, rcnt, gpRestarts, testLength, subject, clientName
     '9F8817580B4A4EAAB59FB6DEA0934223', '2B20031D4BD842A89963F6FFA44A9271', 'A27DAB06891648B3AB996E95EC7AE3BF', '(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016', '168', 'SBAC-IRP-Perf-MATH-3', '(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016', '1', 'started', '2015-12-23 23:23:13.028', '2015-12-23 23:30:15.409', '1', '1', '4', 'MATH', 'SBAC_PT'
     */
    @Test
    public void should_Get_a_TestOpportunity() {
        UUID key = UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934223");
        UUID expectedSessionKey = UUID.fromString("2B20031D-4BD8-42A8-9963-F6FFA44A9271");
        UUID expectedBrowserKey = UUID.fromString("A27DAB06-8916-48B3-AB99-6E95EC7AE3BF");

        TestOpportunity result = testOpportunityDao.get(key);

        Assert.assertNotNull(result);
        Assert.assertEquals(key, result.getKey());
        Assert.assertEquals(expectedSessionKey, result.getSessionKey());
        Assert.assertEquals(expectedBrowserKey, result.getBrowserKey());
        Assert.assertEquals("(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016", result.getTestKey());
        Assert.assertEquals(168d, result.getTestee());
        Assert.assertEquals("SBAC-IRP-Perf-MATH-3", result.getTestId());
        Assert.assertEquals("(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016", result.getAdminSubject());
        Assert.assertEquals((Integer)1, result.getOpportunity());
        Assert.assertEquals("started", result.getStatus());
        Assert.assertEquals("MATH", result.getSubject());
        Assert.assertEquals("SBAC_PT", result.getClientName());
    }

    @Test
    public void should_Return_Null_When_a_TestOpportunity_is_Not_Found() {
        UUID key = UUID.randomUUID();

        TestOpportunity result = testOpportunityDao.get(key);

        Assert.assertNull(result);
    }
}
