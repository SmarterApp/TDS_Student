package tds.student.performance.dao;

import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.TestAbility;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.utils.UuidAdapter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tests for {@code TestOpportunityDao} implementations.
 */
public class TestAbilityDaoTest extends IntegrationTest {
    private final String clientName = "SBAC_PT";
    private final String subject = "MATH";
    private final String testee = "99999";
    private final Timestamp dateScored = new Timestamp(System.currentTimeMillis());
    private final Integer score = 100;
    private final Integer opportunity = 100;
    private final String testId = "SBAC-IRP-Perf-MATH-3";
    private final UUID otherOppKey = UUID.fromString("9f881758-0b4c-4eaa-b59f-b6ddd0934223");
    private final String adminSubject = "AdminSubject";


    @Autowired
    TestAbilityDao testAbilityDao;

    @Before
    public void setup() {
        Map<String, Object> params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(otherOppKey));
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

    }

    @After
    public void cleanup() {
        Map<String, Object> params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(otherOppKey));
        final String SQL =
                "DELETE FROM session.testopportunity\n" +
                        "WHERE _Key = :oppKey";

        namedParameterJdbcTemplate.update(SQL, params);
    }

    /**
     * Record used for testing:
     * # key, sessionKey, browserKey, testKey, testee, testId, test, opportunity, status, dateStarted, dateChanged, rcnt, gpRestarts, testLength, subject, clientName
     '9F8817580B4A4EAAB59FB6DEA0934223', '2B20031D4BD842A89963F6FFA44A9271', 'A27DAB06891648B3AB996E95EC7AE3BF', '(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016', '168', 'SBAC-IRP-Perf-MATH-3', '(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016', '1', 'started', '2015-12-23 23:23:13.028', '2015-12-23 23:30:15.409', '1', '1', '4', 'MATH', 'SBAC_PT'
     */
    @Test
    public void getTestAbilitiesTest() {
        UUID key = UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934223");

        //List<TestAbility> testAbilities = testAbilityDao.getTestAbilities();
    }

}
