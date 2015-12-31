package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.TestOpportunityAudit;
import tds.student.performance.utils.DateUtility;
import tds.student.performance.utils.UuidAdapter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tests for {@code TestOpportunityAuditDao} implementations.
 */
public class TestOpportunityAuditDaoTest extends IntegrationTest {
    @Autowired
    TestOpportunityAuditDao testOpportunityAuditDao;

    @Autowired
    DateUtility dateUtility;

    /**
     * Currently, the assert on this test fails because the opportunityKey and sessionKey values do not have legit
     * statuses, therefore the INSERT... SELECT statement in TestOpportunityAuditDao.create doesn't insert any records.
     * The SQL in TestOpportunityAuditDao.create is valid, but still need to come up with an accurate unit test.
     */
    @Test
    public void should_Create_TestOpportunityAudit_Records_For_Specified_TestOpportunity() {
        UUID opportunityKey = UUID.fromString("f42fc375-f78e-4cdd-9bd6-e67bf0b1bbcf");
        UUID sessionKey = UUID.fromString("f23784e9-6e6f-492c-bf48-819503d07d17");
        Timestamp dateAccessed = dateUtility.getTimestamp();

        TestOpportunityAudit auditRecord = new TestOpportunityAudit(
                opportunityKey,
                dateAccessed,
                "unittest",
                sessionKey,
                "unittest host",
                "unittest_db"
        );

        testOpportunityAuditDao.create(auditRecord);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(opportunityKey));
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(sessionKey));
        parameters.put("dateAccessed", dateAccessed);

        final String SQL = "SELECT COUNT(*) AS count FROM archive.opportunityaudit WHERE _fk_testopportunity = :key AND _fk_session = :sessionKey AND dateaccessed = :dateAccessed";
        final Integer result = namedParameterJdbcTemplate.queryForInt(SQL, parameters);

        Assert.assertEquals((Integer)1, result);
    }
}
