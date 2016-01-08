package tds.student.performance.dao;

import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.TestAbility;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.utils.UuidAdapter;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@code TestOpportunityDao} implementations.
 */
public class TestAbilityDaoTest extends IntegrationTest {
    private final String clientName = "SBAC_PT";
    private final String subject = "MATH";
    private final Long testee = 9999L;
    private final Timestamp dateScored = new Timestamp(System.currentTimeMillis());
    private final Float score = 100F;
    private final Integer opportunity = 100;
    private final String testId = "SBAC-IRP-Perf-MATH-3";
    private final UUID otherOppKey = UUID.randomUUID();
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

        params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(otherOppKey));
        params.put("score", score);
        params.put("date", new Date());

        final String TEST_SCORE_SQL =
            "INSERT INTO session.testopportunityscores (_fk_testopportunity, measurelabel, measureof, value, useforability, _date) " +
                    "VALUES (:oppKey, 'measure label', 'measure of', :score, 1, :date)";

        namedParameterJdbcTemplate.update(TEST_SCORE_SQL, params);

        params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(otherOppKey));
        params.put("score", score);
        params.put("testee", testee);
        params.put("clientName", clientName);
        params.put("subject", subject);

        final String TESTEE_HISTORY_SQL =
                "INSERT INTO session.testeehistory (_Key, _efk_Testee, clientname, Subject, initialAbility) " +
                        "VALUES (:oppKey, :testee, :clientName, :subject, :score)";

        namedParameterJdbcTemplate.update(TESTEE_HISTORY_SQL, params);
    }

    @After
    public void cleanup() {
        Map<String, Object> params = new HashMap<>();
        params.put("oppKey", UuidAdapter.getBytesFromUUID(otherOppKey));
        final String DELETE_SQL1 =
                "DELETE FROM session.testopportunityscores\n" +
                        "WHERE _fk_testopportunity = :oppKey";

        final String DELETE_SQL2 =
                "DELETE FROM session.testopportunity\n" +
                        "WHERE _Key = :oppKey";

        final String DELETE_SQL3 =
                "DELETE FROM session.testeehistory\n" +
                        "WHERE _efk_Testee = :testee";

        params.put("testee", testee);
        namedParameterJdbcTemplate.update(DELETE_SQL1, params);
        namedParameterJdbcTemplate.update(DELETE_SQL2, params);
        namedParameterJdbcTemplate.update(DELETE_SQL3, params);
    }

    @Test
    public void getTestAbilitiesTest() {
        UUID key = UUID.randomUUID();

        List<TestAbility> testAbilities = testAbilityDao.getTestAbilities(key, clientName, subject, testee);
        assertNotNull(testAbilities);
        assertTrue(testAbilities.size() == 1);
        assertEquals(testAbilities.get(0).getOppkey(), otherOppKey);
        assertEquals(testAbilities.get(0).getOpportunity(), opportunity);
        assertEquals(testAbilities.get(0).getScore(), score);
        assertEquals(testAbilities.get(0).getTest(), testId);
    }

    @Test
    public void getMostRecentTestAbilityFromHistoryTest() {
        Float ability = testAbilityDao.getMostRecentTestAbilityFromHistory(clientName, subject, testee);
        assertEquals(ability, score);
    }

}
