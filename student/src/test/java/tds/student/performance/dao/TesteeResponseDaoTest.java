package tds.student.performance.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.UnfinishedResponsePage;
import tds.student.performance.utils.UuidAdapter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

/**
 * Created by emunoz on 12/30/15.
 */
public class TesteeResponseDaoTest extends IntegrationTest {
    private final UUID oppKey = UUID.randomUUID();
    private final String clientName = "SBAC_PT";
    private final String subject = "MATH";
    private final Long testee = 9999L;
    private final Timestamp dateScored = new Timestamp(System.currentTimeMillis());
    private final Float score = 100F;
    private final Integer opportunity = 100;
    private final String testId = "SBAC-IRP-Perf-MATH-3";
    private final String adminSubject = "AdminSubject";
    private final Integer position = 6;
    private final Integer page = 3;

    @Autowired
    TesteeResponseDao testeeResponseDao;

    @Before
    public void setup() {
        Map<String, Object> params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));
        params.put("testee", testee);
        params.put("subject", subject);
        params.put("testid", testId);
        params.put("dateScored", dateScored);
        params.put("opportunity", opportunity);
        params.put("clientName", clientName);
        params.put("adminSubject", adminSubject);

        final String TEST_OPP_SQL =
                "INSERT INTO session.testopportunity (_Key, _efk_Testee, _efk_TestID, opportunity, dateScored, subject, " +
                        "clientname, _version, _efk_adminsubject, environment, isSegmented, algorithm) " +
                        "VALUES (:oppKey, :testee, :testid, :opportunity, :dateScored, :subject, :clientName, 1, :adminSubject, 'dev', 0, 'fixedform')";

        namedParameterJdbcTemplate.update(TEST_OPP_SQL, params);

        params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));
        params.put("position", position);
        params.put("dateGenerated", dateScored);
        params.put("page", page);

        final String SQL =
                "INSERT INTO session.testeeresponse (_fk_testopportunity, position, dategenerated, page) " +
                        "VALUES (:oppKey, :position, :dateGenerated, :page)";

        namedParameterJdbcTemplate.update(SQL, params);
}

    @After
    public void cleanup() {
        Map<String, Object> params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));

        final String DELETE_SQL1 =
                "DELETE FROM session.testeeresponse WHERE _fk_testopportunity = :oppKey";

        final String DELETE_SQL2 =
                "DELETE FROM session.testopportunity\n" +
                        "WHERE _Key = :oppKey";


        namedParameterJdbcTemplate.update(DELETE_SQL1, params);
        namedParameterJdbcTemplate.update(DELETE_SQL2, params);
    }

    @Test
    public void testUpdateRestartCount() {
        List<UnfinishedResponsePage> pages = testeeResponseDao.getUnfinishedPages(oppKey);
        assertNotNull(pages);
    }
}
