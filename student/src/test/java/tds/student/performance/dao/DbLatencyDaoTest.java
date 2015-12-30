package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import tds.student.performance.IntegrationTest;
import tds.student.performance.utils.DateUtility;
import tds.student.performance.utils.UuidAdapter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tests for {@code DbLatencyDao} implementations.
 */
public class DbLatencyDaoTest extends IntegrationTest {
    @Autowired
    private DbLatencyDao dbLatencyDao;

    @Autowired
    DateUtility dateUtility;

    @Test
    @Rollback(false)
    public void should_Create_a_New_DbLatency_Record() {
        UUID mockTestOppKey = UUID.randomUUID();
        UUID mockTestSessionKey = UUID.randomUUID();
        Integer n = ThreadLocalRandom.current().nextInt(1, 100);
        String procName = "should_Create_a_New_DbLatency_Record";

        Date endDate = dateUtility.getCurrentDbDate();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, -10);
        Date startDate = cal.getTime();

        long duration = endDate.getTime() - startDate.getTime();

        dbLatencyDao.create(
                procName,
                duration,
                startDate,
                new Date(duration),
                Long.valueOf(1),
                n,
                mockTestOppKey,
                mockTestSessionKey,
                "SBAC_TEST",
                "this is from the test"
        );

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("procName", procName);
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(mockTestSessionKey));
        parameters.put("testOppKey", UuidAdapter.getBytesFromUUID(mockTestOppKey));
        parameters.put("startTime", startDate);
        parameters.put("n", n);

        final String SQL = "SELECT COUNT(*) AS count FROM archive._dblatency WHERE procName = :procName AND _fk_session = :sessionKey AND _fk_testopportunity = :testOppKey AND starttime = :startTime AND n = :n";

        Integer expectedValue = 1;
        Integer count = namedParameterJdbcTemplate.queryForInt(SQL, parameters);
        Assert.assertEquals(expectedValue, count);
    }
}
