package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.SessionAudit;
import tds.student.performance.domain.TestSession;
import tds.student.performance.domain.TestSessionTimeLimitConfiguration;
import tds.student.performance.utils.UuidAdapter;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;


/**
 * Tests for {@code TestSessionDao} implementations.
 */
public class TestSessionDaoTest extends IntegrationTest {
    @Autowired
    TestSessionDao testSessionDao;

    @Test
    public void should_Get_a_TestSession_For_Specified_Key() {
        final UUID expectedKey = UUID.randomUUID();
        final String expectedStatus = "open";
        final Integer expectedSessionType = 0;
        final Timestamp expectedDateCreated = new Timestamp(System.currentTimeMillis());
        final Timestamp expectedDateBegin = new Timestamp(System.currentTimeMillis());
        final Timestamp expectedDateEnd = new Timestamp(System.currentTimeMillis());
        final Timestamp expectedDateVisited = new Timestamp(System.currentTimeMillis());
        final String expectedClientName = "SBAC_PT";
        final Long expectedEfkProctor = 93L;
        final String expectedProctorId = "u7gvfxw1pseyue7k1hed36";
        final String expectedProctorName = "Paulie Proctor36";
        final String expectedSessionId = "UNT-TST-001";
        final String expectedEnvironment = "unit-test";
        final UUID expectedSessionBrowserKey = UUID.randomUUID();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(expectedKey));
        parameters.put("efkProctor", expectedEfkProctor);
        parameters.put("proctorId", expectedProctorId);
        parameters.put("proctorName", expectedProctorName);
        parameters.put("sessionId", expectedSessionId);
        parameters.put("status", expectedStatus);
        parameters.put("dateCreated", expectedDateCreated);
        parameters.put("dateBegin", expectedDateBegin);
        parameters.put("dateEnd", expectedDateEnd);
        parameters.put("dateVisited", expectedDateVisited);
        parameters.put("clientName", expectedClientName);
        parameters.put("sessionBrowser", UuidAdapter.getBytesFromUUID(expectedSessionBrowserKey));
        parameters.put("environment", expectedEnvironment);
        parameters.put("sessionType", expectedSessionType);

        final String SQL =
                "INSERT INTO session.session(_key, _efk_proctor, proctorid, proctorname, sessionid, status, datecreated, datebegin, dateend, datevisited, clientname, _fk_browser, environment, sessiontype)\n" +
                "VALUES (:key, :efkProctor, :proctorId, :proctorName, :sessionId, :status, :dateCreated, :dateBegin, :dateEnd, :dateVisited, :clientName, :sessionBrowser, :environment, :sessionType)";

        namedParameterJdbcTemplate.update(SQL, parameters);

        TestSession result = testSessionDao.get(expectedKey);

        Assert.assertNotNull(result);
        Assert.assertEquals(expectedKey, result.getKey());
        Assert.assertEquals(expectedEfkProctor, result.getProctorId());
        Assert.assertEquals(expectedProctorName, result.getProctorName());
        Assert.assertEquals(expectedSessionType, result.getSessionType());
        Assert.assertEquals(expectedStatus, result.getStatus());
        Assert.assertEquals(expectedClientName, result.getClientName());
        Assert.assertEquals(expectedDateBegin, result.getDateBegin());
        Assert.assertEquals(expectedDateEnd, result.getDateEnd());
        Assert.assertEquals(expectedDateVisited, result.getDateVisited());
        Assert.assertEquals(expectedSessionId, result.getSessionId());
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
        TestSession testSession = new TestSession();
        testSession.setDateBegin(new Timestamp(System.currentTimeMillis()));
        testSession.setDateEnd(new Timestamp(getDateAddSeconds(3600).getTime()));
        testSession.setStatus("open");

        Assert.assertTrue(testSession.isOpen(new Date()));
    }

    @Test
    public void should_Not_Be_an_Open_TestSession_When_Time_Has_Passed_and_Status_is_Closed() {
        TestSession testSession = new TestSession();
        testSession.setDateBegin(new Timestamp(getDateAddSeconds(-7200).getTime()));
        testSession.setDateEnd(new Timestamp(getDateAddSeconds(-3600).getTime()));
        testSession.setStatus("closed");

        Assert.assertTrue(!testSession.isOpen(new Date()));
    }

    @Test
    public void should_Not_Be_an_Open_TestSession_When_Time_Has_Passed_and_Status_is_Open() {
        TestSession testSession = new TestSession();
        testSession.setDateBegin(new Timestamp(getDateAddSeconds(-7200).getTime()));
        testSession.setDateEnd(new Timestamp(getDateAddSeconds(-3600).getTime()));
        testSession.setStatus("open");

        Assert.assertTrue(!testSession.isOpen(new Date()));
    }

    @Test
    public void should_Not_Be_an_Open_TestSession_When_Time_Has_Not_Passed_and_Status_is_Closed() {
        TestSession testSession = new TestSession();
        testSession.setDateBegin(new Timestamp(System.currentTimeMillis()));
        testSession.setDateEnd(new Timestamp(getDateAddSeconds(3600).getTime()));
        testSession.setStatus("closed");

        Assert.assertTrue(!testSession.isOpen(new Date()));
    }

    /**
     * Record used for testing:
     * # _key, _efk_testid, oppexpire, opprestart, oppdelay, interfacetimeout, clientname, ispracticetest, refreshvalue, tainterfacetimeout, tacheckintime, datechanged, datepublished, environment, sessionexpire, requestinterfacetimeout, refreshvaluemultiplier
     ?, NULL, '1', '10', '-1', '10', 'SBAC_PT', '1', '30', '20', '20', '2012-12-21 00:02:53.000', '2012-12-21 00:02:53.000', NULL, '8', '15', '2'
     */
    @Test
    public void should_Return_a_TestSessionTimeLimitConfiguration_With_Null_TestId_For_SBAC_PT_ClientName() {
        String clientName = "SBAC_PT";

        TestSessionTimeLimitConfiguration result = testSessionDao.getTimeLimitConfiguration(clientName);

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
    public void should_Return_a_CheckInTimeLimit_of_20_For_ClientName_SBAC_PT() {
        String clientName = "SBAC_PT";

        TestSessionTimeLimitConfiguration result = testSessionDao.getTimeLimitConfiguration(clientName);

        Integer checkin = result.getTaCheckinTime();

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

    @Test
    public void should_Create_a_New_SessionAudit_Record() {
        UUID mockSessionKey = UUID.randomUUID();
        UUID mockBrowserKey = UUID.randomUUID();
        Timestamp mockDate = new Timestamp(new Date().getTime());

        testSessionDao.createAudit(new SessionAudit(
                mockSessionKey,
                mockDate,
                "unittest",
                "unittest host",
                mockBrowserKey,
                "unittest_db"
        ));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(mockSessionKey));
        parameters.put("browserKey", UuidAdapter.getBytesFromUUID(mockBrowserKey));
        parameters.put("date", mockDate);

        final String SQL = "SELECT COUNT(*) AS count FROM archive.sessionaudit WHERE _fk_session = :key AND browserkey = :browserKey AND dateaccessed = :date";
        final Integer result = namedParameterJdbcTemplate.queryForInt(SQL, parameters);

        Assert.assertEquals((Integer)1, result);
    }
}
