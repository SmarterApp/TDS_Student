package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.TestSession;
import tds.student.performance.domain.TestSessionTimeLimitConfiguration;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Tests for {@code TestSessionDao} implementations.
 */
public class TestSessionDaoTest extends IntegrationTest {
    @Autowired
    TestSessionDao testSessionDao;

    /**
     * Record used for testing:
     # key, status, dateBegin, dateEnd, dateVisited, clientName, proctor, sessionBrowser
     '2B20031D4BD842A89963F6FFA44A9271', 'open', '2015-12-23 23:00:03.253', '2015-12-24 07:00:03.253', '2015-12-23 23:45:51.459', 'SBAC_PT', '93', '9DE1471D0A834A4FA3810B1D7EC24E4A'
     */
    @Test
    public void should_Get_a_TestSession_For_Specified_Key() {
        UUID key = UUID.fromString("2B20031D-4BD8-42A8-9963-F6FFA44A9271");
        UUID expectedSessionBrowserKey = UUID.fromString("9DE1471D-0A83-4A4F-A381-0B1D7EC24E4A");

        TestSession result = testSessionDao.get(key);

        Assert.assertNotNull(result);
        Assert.assertEquals(key, result.getKey());
        Assert.assertEquals((Integer)0, result.getSessionType());
        Assert.assertEquals("open", result.getStatus());
        Assert.assertEquals("SBAC_PT", result.getClientName());
        Assert.assertEquals(Long.valueOf(93), result.getProctorId());
        Assert.assertEquals(expectedSessionBrowserKey, result.getSessionBrowser());
    }

    @Test
    public void should_Return_Null_When_a_TestSession_is_Not_Found() {
        UUID key = UUID.randomUUID();

        TestSession result = testSessionDao.get(key);

        Assert.assertNull(result);
    }

    @Test
    public void should_Be_an_Open_TestSession() {
        // TODO:  create test.
    }

    @Test
    public void should_Not_Be_an_Open_TestSession() {
        TestSession testSession = testSessionDao.get(UUID.fromString("2B20031D-4BD8-42A8-9963-F6FFA44A9271"));

        Assert.assertTrue(!testSession.isOpen(new Date()));
    }

    /**
     * Record used for testing:
     * # _key, _efk_testid, oppexpire, opprestart, oppdelay, interfacetimeout, clientname, ispracticetest, refreshvalue, tainterfacetimeout, tacheckintime, datechanged, datepublished, environment, sessionexpire, requestinterfacetimeout, refreshvaluemultiplier
     ?, NULL, '1', '10', '-1', '10', 'SBAC_PT', '1', '30', '20', '20', '2012-12-21 00:02:53.000', '2012-12-21 00:02:53.000', NULL, '8', '15', '2'
     */
    @Test
    public void should_Return_a_TestSessionTimeLimitConfiguration_for_SBAC_PT_ClientName_and_Null_TestId() {
        String clientName = "SBAC_PT";

        List<TestSessionTimeLimitConfiguration> result = testSessionDao.getTimeLimitConfigurations(clientName, null);

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());

        TestSessionTimeLimitConfiguration config = result.get(0);
        Assert.assertEquals(null, config.getTestId());
        Assert.assertEquals((Integer)1, config.getOpportunityExpiration());
        Assert.assertEquals((Integer)10, config.getOpportunityRestart());
        Assert.assertEquals(Integer.valueOf(-1), config.getOpportunityDelay());
        Assert.assertEquals((Integer)10, config.getInterfaceTimeout());
        Assert.assertEquals((Integer)15, config.getRequestInterfaceTimeout());
        Assert.assertEquals(clientName, config.getClientName());
        Assert.assertEquals("dev", config.getEnvironment());
        Assert.assertEquals(true, config.getIsPracticeTest());
        Assert.assertEquals((Integer)30, config.getRefreshValue());
        Assert.assertEquals((Integer)20, config.getTaInterfaceTimeout());
        Assert.assertEquals((Integer)20, config.getTaCheckinTime());
        Assert.assertEquals((Integer)8, config.getSessionExpiration());
        Assert.assertEquals((Integer)2, config.getRefreshValueMultiplier());
    }

    /**
     * Record used for testing:
     * # _key, _efk_testid, oppexpire, opprestart, oppdelay, interfacetimeout, clientname, ispracticetest, refreshvalue, tainterfacetimeout, tacheckintime, datechanged, datepublished, environment, sessionexpire, requestinterfacetimeout, refreshvaluemultiplier
     ?, NULL, '1', '10', '-1', '10', 'SBAC_PT', '1', '30', '20', '20', '2012-12-21 00:02:53.000', '2012-12-21 00:02:53.000', NULL, '8', '15', '2'
     */
    @Test
    public void should_Return_a_TestSessionTimeLimitConfiguration_With_Null_TestId_For_SBAC_PT_ClientName_and_SBAC_Math_3_MATH_3_TestId() {
        String clientName = "SBAC_PT";
        String testId = "SBAC Math 3-MATH-3";

        List<TestSessionTimeLimitConfiguration> result = testSessionDao.getTimeLimitConfigurations(clientName, testId);

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());

        TestSessionTimeLimitConfiguration config = result.get(0);
        Assert.assertEquals(null, config.getTestId());
        Assert.assertEquals((Integer)1, config.getOpportunityExpiration());
        Assert.assertEquals((Integer)10, config.getOpportunityRestart());
        Assert.assertEquals(Integer.valueOf(-1), config.getOpportunityDelay());
        Assert.assertEquals((Integer)10, config.getInterfaceTimeout());
        Assert.assertEquals((Integer)15, config.getRequestInterfaceTimeout());
        Assert.assertEquals(clientName, config.getClientName());
        Assert.assertEquals("dev", config.getEnvironment());
        Assert.assertEquals(true, config.getIsPracticeTest());
        Assert.assertEquals((Integer)30, config.getRefreshValue());
        Assert.assertEquals((Integer)20, config.getTaInterfaceTimeout());
        Assert.assertEquals((Integer)20, config.getTaCheckinTime());
        Assert.assertEquals((Integer)8, config.getSessionExpiration());
        Assert.assertEquals((Integer)2, config.getRefreshValueMultiplier());
    }

    @Test
    public void should_Return_a_CheckInTimeLimit_of_20_For_ClientName_SBAC_PT() {
        String clientName = "SBAC_PT";
        String testId = null;

        List<TestSessionTimeLimitConfiguration> result = testSessionDao.getTimeLimitConfigurations(clientName, testId);

        TestSessionTimeLimitConfiguration timelimitToFind = new TestSessionTimeLimitConfiguration(clientName, testId);

        TestSessionTimeLimitConfiguration item = result.contains(timelimitToFind)
                ? result.get(result.indexOf(timelimitToFind))
                : result.get(0);

        Integer checkin = item.getTaCheckinTime();

        Assert.assertNotNull(checkin);
        Assert.assertEquals((Integer)20, checkin);
    }

    @Test
    public void should_Pause_an_Open_TestSession() {
        // TODO:  create test.
    }

    @Test
    public void should_Return_Null_Validate_Proctor_Is_Valid() {
        UUID sessionKey = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();

        Timestamp begin = new Timestamp(getDateAddSeconds(-120).getTime());
        Timestamp end = new Timestamp(getDateAddSeconds(120).getTime());

        TestSession testSession = new TestSession();
        testSession.setKey(sessionKey);
        testSession.setSessionBrowser(browserKey);
        testSession.setProctorId(99L);
        testSession.setDateBegin(begin);
        testSession.setDateEnd(end);

        String msg = testSessionDao.validateProctorSession(testSession);
        Assert.assertNull(msg);

        msg = testSessionDao.validateProctorSession(testSession, testSession.getProctorId(), testSession.getSessionBrowser());
        Assert.assertNull(msg);
    }

    @Test
    public void should_Return_Closed_Message_From_Validate_Proctor_When_Invalid_Date() {
        UUID sessionKey = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();

        Timestamp begin = new Timestamp(getDateAddSeconds(60).getTime());
        Timestamp end = new Timestamp(getDateAddSeconds(120).getTime());

        TestSession testSession = new TestSession();
        testSession.setKey(sessionKey);
        testSession.setSessionBrowser(browserKey);
        testSession.setProctorId(99L);
        testSession.setDateBegin(begin);
        testSession.setDateEnd(end);

        String msg = testSessionDao.validateProctorSession(testSession);
        Assert.assertEquals("The session is closed.", msg);

        begin = new Timestamp(getDateAddSeconds(-120).getTime());
        end = new Timestamp(getDateAddSeconds(-60).getTime());

        testSession.setDateBegin(begin);
        testSession.setDateEnd(end);

        msg = testSessionDao.validateProctorSession(testSession);
        Assert.assertEquals("The session is closed.", msg);
    }

    @Test
    public void should_Return_Invalid_Proctor_Message_From_Validate_Proctor() {
        UUID sessionKey = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();

        Timestamp begin = new Timestamp(getDateAddSeconds(-60).getTime());
        Timestamp end = new Timestamp(getDateAddSeconds(120).getTime());

        TestSession testSession = new TestSession();
        testSession.setKey(sessionKey);
        testSession.setSessionBrowser(browserKey);
        testSession.setProctorId(99L);
        testSession.setDateBegin(begin);
        testSession.setDateEnd(end);

        String msg = testSessionDao.validateProctorSession(testSession, 1L, browserKey);
        Assert.assertEquals("The session is not owned by this proctor", msg);
    }

    @Test
    public void should_Return_Unauthorized_Access_Message_From_Validate_Proctor() {
        UUID sessionKey = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();

        Timestamp begin = new Timestamp(getDateAddSeconds(-60).getTime());
        Timestamp end = new Timestamp(getDateAddSeconds(120).getTime());

        TestSession testSession = new TestSession();
        testSession.setKey(sessionKey);
        testSession.setSessionBrowser(browserKey);
        testSession.setProctorId(99L);
        testSession.setDateBegin(begin);
        testSession.setDateEnd(end);

        String msg = testSessionDao.validateProctorSession(testSession, testSession.getProctorId(), UUID.randomUUID());
        Assert.assertEquals("Unauthorized session access", msg);
    }
}
