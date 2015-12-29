package tds.student.performance.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import tds.student.performance.domain.TestSession;
import tds.student.performance.domain.TestSessionTimeLimitConfiguration;

import java.util.UUID;

/**
 * Created by jjohnson on 12/27/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:performance-integration-context.xml")
@TransactionConfiguration
public class TestSessionServiceTest {
    @Autowired
    TestSessionService testSessionService;

    @Test
    public void should_get_a_TestSession_For_a_Valid_Key() {
        UUID key = UUID.fromString("2B20031D-4BD8-42A8-9963-F6FFA44A9271");
        UUID expectedSessionBrowserKey = UUID.fromString("9DE1471D-0A83-4A4F-A381-0B1D7EC24E4A");

        TestSession result = testSessionService.get(key);

        Assert.assertNotNull(result);
        Assert.assertEquals(key, result.getKey());
        Assert.assertEquals((Integer)0, result.getSessionType());
        Assert.assertEquals("open", result.getStatus());
        Assert.assertEquals("SBAC_PT", result.getClientName());
        Assert.assertEquals((Double)93d, result.getProctorId());
        Assert.assertEquals(expectedSessionBrowserKey, result.getSessionBrowser());
    }

    @Test
    public void should_Return_a_CheckIn_Time_of_20_For_SBAC_PT() {
        String clientName = "SBAC_PT";

        Integer result = testSessionService.getCheckInTimeLimit(clientName);

        Assert.assertNotNull(result);
        Assert.assertEquals((Integer)20, result);
    }

    @Test
    public void should_Return_a_TestSessionTimeoutConfiguration_For_SBAC_PT_and_Null_TestId() {
        String clientName = "SBAC_PT";

        TestSessionTimeLimitConfiguration result = testSessionService.getTimelimitConfiguration(clientName, null);

        Assert.assertNotNull(result);
        Assert.assertEquals(null, result.getTestId());
        Assert.assertEquals((Integer)1, result.getOpportunityExpiration());
        Assert.assertEquals((Integer)10, result.getOpportunityRestart());
        Assert.assertEquals(Integer.valueOf(-1), result.getOpportunityDelay());
        Assert.assertEquals((Integer)10, result.getInterfaceTimeout());
        Assert.assertEquals((Integer)15, result.getRequestInterfaceTimeout());
        Assert.assertEquals(clientName, result.getClientName());
        Assert.assertEquals("dev", result.getEnvironment());
        Assert.assertEquals(true, result.getIsPracticeTest());
        Assert.assertEquals((Integer)30, result.getRefreshValue());
        Assert.assertEquals((Integer)20, result.getTaInterfaceTimeout());
        Assert.assertEquals((Integer)20, result.getTaCheckinTime());
        Assert.assertEquals((Integer)8, result.getSessionExpiration());
        Assert.assertEquals((Integer)2, result.getRefreshValueMultiplier());
    }

    @Test
    public void should_Return_a_TestSessionTimeoutConfiguration_With_Null_TestId_For_SBAC_PT_ClientName_and_SBAC_Math_3_MATH_3_TestId() {
        String clientName = "SBAC_PT";
        String testId = "SBAC Math 3-MATH-3";

        TestSessionTimeLimitConfiguration result = testSessionService.getTimelimitConfiguration(clientName, testId);

        Assert.assertNotNull(result);
        Assert.assertEquals(null, result.getTestId());
        Assert.assertEquals((Integer)1, result.getOpportunityExpiration());
        Assert.assertEquals((Integer)10, result.getOpportunityRestart());
        Assert.assertEquals(Integer.valueOf(-1), result.getOpportunityDelay());
        Assert.assertEquals((Integer)10, result.getInterfaceTimeout());
        Assert.assertEquals((Integer)15, result.getRequestInterfaceTimeout());
        Assert.assertEquals(clientName, result.getClientName());
        Assert.assertEquals("dev", result.getEnvironment());
        Assert.assertEquals(true, result.getIsPracticeTest());
        Assert.assertEquals((Integer)30, result.getRefreshValue());
        Assert.assertEquals((Integer)20, result.getTaInterfaceTimeout());
        Assert.assertEquals((Integer)20, result.getTaCheckinTime());
        Assert.assertEquals((Integer)8, result.getSessionExpiration());
        Assert.assertEquals((Integer)2, result.getRefreshValueMultiplier());
    }
}
