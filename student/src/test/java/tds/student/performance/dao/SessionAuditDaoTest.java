package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.SessionAudit;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tests for {@code SessionAuditDao} implementations.
 */

public class SessionAuditDaoTest extends IntegrationTest {
    @Autowired
    private SessionAuditDao sessionAuditDao;

    @Test
    public void should_Create_a_New_SessionAudit_Record() {
        UUID mockSessionKey = UUID.randomUUID();
        UUID mockBrowserKey = UUID.randomUUID();
        Timestamp mockDate = new Timestamp(new Date().getTime());

        sessionAuditDao.create(new SessionAudit(
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
