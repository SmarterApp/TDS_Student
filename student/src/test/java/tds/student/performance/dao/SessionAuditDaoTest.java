package tds.student.performance.dao;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.SessionAudit;

import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tests for {@code SessionAuditDao} implementations.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/performance-integration-context.xml")
@TransactionConfiguration
public class SessionAuditDaoTest {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Autowired
    private SessionAuditDao sessionAuditDao;

    @Test
    public void should_Create_a_New_SessionAudit_Record() {
        UUID mockSessionKey = UUID.randomUUID();
        UUID mockBrowserKey = UUID.randomUUID();
        Date mockDate = new Date();

        sessionAuditDao.create(new SessionAudit(
                mockSessionKey,
                mockDate,
                "unittest",
                "unittest host", // Wouldn't we want the IP address of the user who tried to start a timed-out session?
                mockBrowserKey,
                "unittest_db"
        ));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(mockSessionKey));
        parameters.put("browserKey", UuidAdapter.getBytesFromUUID(mockBrowserKey));
        parameters.put("date", mockDate);

        final String SQL = "SELECT COUNT(*) AS count FROM archive.sessionaudit WHERE _fk_session = :key AND browserkey = :browserKey AND dateaccessed = :date";
        final SqlRowSet result = namedParameterJdbcTemplate.queryForRowSet(SQL, parameters);

        Assert.assertNotNull(result);
        while (result.next()) {
            Assert.assertEquals(1, result.getInt("count"));
        }
    }
}
