package tds.student.performance.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import tds.student.performance.dao.DbLatencyDao;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSession;
import tds.student.performance.utils.SqlHelper;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:performance-integration-context.xml")
@TransactionConfiguration
public class DbLatencyServiceTest {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Autowired
    DbLatencyService dbLatencyService;


    @Test
    public void should_Insert_Latency_Record_For_TestOpportunity() {
        UUID testOppKey = UUID.randomUUID();
        UUID sessionKey = UUID.randomUUID();
        Date startDate = getDateSecondsAgo(11);
        String procName = "should_Insert_Latency_Record_For_TestOpportunity";
        Integer n = ThreadLocalRandom.current().nextInt(1, 100);

        TestOpportunity testOpportunity = new TestOpportunity();
        testOpportunity.setClientName("SBAC_TEST");
        testOpportunity.setKey(testOppKey);
        testOpportunity.setSessionKey(sessionKey);

        dbLatencyService.logLatency(procName, startDate, Long.valueOf(1), n, testOpportunity, "comments");

        assertInserted(procName, testOppKey, sessionKey, n, startDate);
    }

    @Test
    public void should_Insert_Latency_Record_For_TestSession() {
        UUID sessionKey = UUID.randomUUID();
        Date startDate = getDateSecondsAgo(11);
        String procName = "should_Insert_Latency_Record_For_TestSession";
        Integer n = ThreadLocalRandom.current().nextInt(1, 100);

        TestSession testSession = new TestSession();
        testSession.setClientName("SBAC_TEST");
        testSession.setKey(sessionKey);

        dbLatencyService.logLatency(procName, startDate, Long.valueOf(1), n, testSession, "comments");

        assertInserted(procName, null, sessionKey, n, startDate);
    }

    @Test
    public void should_Insert_Latency_Record_For_Generic() {
        Date startDate = getDateSecondsAgo(11);
        String procName = "should_Insert_Latency_Record_For_Generic";
        Integer n = ThreadLocalRandom.current().nextInt(1, 100);
        String clientName = "SBAC_TEST";

        dbLatencyService.logLatency(procName, startDate, Long.valueOf(1), n, clientName, "comments");

        assertInserted(procName, null, null, n, startDate);
    }

    @Test
    public void should_Not_Insert_Latency_Record_When_Disabled() {
        Date startDate = getDateSecondsAgo(11);
        String procName = "should_Not_Insert_Latency_Record_When_Disabled";
        Integer n = ThreadLocalRandom.current().nextInt(1, 100);
        String clientName = "SBAC_TEST";

        dbLatencyService.setEnabled(false);

        dbLatencyService.logLatency(procName, startDate, Long.valueOf(1), n, clientName, "comments");
        assertNotInserted(procName, null, null, n, startDate);

        dbLatencyService.setEnabled(true);

        dbLatencyService.logLatency(procName, startDate, Long.valueOf(1), n, clientName, "comments");
        assertInserted(procName, null, null, n, startDate);
    }

    private void assertInserted(String procName, UUID testOppKey, UUID sessionKey, Integer n, Date startDate) {
        Integer expectedValue = 1;
        Integer count = getDbRecordCount(procName, testOppKey, sessionKey, n, startDate);
        Assert.assertEquals(expectedValue, count);
    }

    private void assertNotInserted(String procName, UUID testOppKey, UUID sessionKey, Integer n, Date startDate) {
        Integer expectedValue = 0;
        Integer count = getDbRecordCount(procName, testOppKey, sessionKey, n, startDate);
        Assert.assertEquals(expectedValue, count);
    }

    private Integer getDbRecordCount(String procName, UUID testOppKey, UUID sessionKey, Integer n, Date startDate) {
        String SQL = String.format(
                "SELECT COUNT(*) AS count FROM archive._dblatency WHERE procName = :procName AND %s AND %s AND starttime = :startTime AND n = :n",
                SqlHelper.createNullCheckedClause("sessionKey", "_fk_session", sessionKey),
                SqlHelper.createNullCheckedClause("testOppKey", "_fk_testopportunity", testOppKey)
        );

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("procName", procName);
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(sessionKey));
        parameters.put("testOppKey", UuidAdapter.getBytesFromUUID(testOppKey));
        parameters.put("startTime", startDate);
        parameters.put("n", n);

        return namedParameterJdbcTemplate.queryForInt(SQL, parameters);
    }

    private Date getDateSecondsAgo(Integer seconds) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, seconds * -1);
        return cal.getTime();
    }
}
