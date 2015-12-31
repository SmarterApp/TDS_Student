package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.TestSessionDao;
import tds.student.performance.dao.mappers.TestSessionMapper;
import tds.student.performance.domain.TestSessionTimeLimitConfiguration;
import tds.student.performance.utils.DateUtility;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.TestSession;

import javax.sql.DataSource;
import java.util.*;

/**
 * Data Access Object for interacting with  {@code TestSession} records.
 */
@Repository
public class TestSessionDaoImpl implements TestSessionDao {
    private static final Logger logger = LoggerFactory.getLogger(TestSessionDaoImpl.class);
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Autowired
    DateUtility dateUtility;

    /**
     * Get a {@code TestSession} for the specified session key.
     * @param key The session key of the {@code TestSession} to be fetched.
     * @return A (@code TestSession} for the specified session key.
     */
    @Override
    @Transactional
    public TestSession get(UUID key) {
        Map<String, byte[]> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(key));

        final String SQL =
                "SELECT\n" +
                    "_key AS `key`,\n" +
                    "sessiontype AS sessionType,\n" +
                    "status AS status,\n" +
                    "datebegin AS dateBegin,\n" +
                    "dateend AS dateEnd,\n" +
                    "datevisited AS dateVisited,\n" +
                    "clientname AS clientName,\n" +
                    "_efk_proctor AS proctor,\n" +
                    "_fk_browser AS sessionBrowser\n" +
                "FROM\n" +
                    "session.session\n" +
                "WHERE\n" +
                    "_key = :key";

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    SQL,
                    parameters,
                    new TestSessionMapper());
        } catch(EmptyResultDataAccessException e) {
            logger.warn(String.format("%s did not return any results for key = %s", SQL, key), e);
            return null;
        }
    }

    /**
     * Get a collection of {@code TestSessionTimeLimitConfiguration} records from the {@code session.timelimits} table.
     * <p>
     *     The logic in {@code StudentDLL.T_StartTestOpportunity_SP} fetches records from {@code session.timelimits} twice:
     *     Once for the clientName and testId.  If no record is returned, the same query is issued against
     *     {@code session.timelimits} again, this time for clientName and NULL testId.  The assumption is the code is
     *     trying to find time limit values associated to the test and falling back to use time limit values associated
     *     to the client.  When the database is seeded, all records have a NULL value for testId.
     * </p>
     * @param clientName The name of the client for which the {@code TestSessionTimeLimitConfiguration} records should be fetched.
     * @param testId The name of the test for which {@code TestSessionTimeLimitConfiguration} records.  Can be {@code null}.
     * @return A {@code List<TestSessionTimeLimitConfiguration>} containing the record(s) for the specified client name and test id.
     */
    @Override
    @Transactional
    public List<TestSessionTimeLimitConfiguration> getTimeLimitConfigurations(String clientName, String testId) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("clientName", clientName);
        parameters.put("testId", testId);

        final String SQL =
                "SELECT\n" +
                    "_efk_testid AS testId,\n" +
                    "oppexpire AS opportunityExpiration,\n" +
                    "opprestart AS opportunityRestart,\n" +
                    "oppdelay AS opportunityDelay,\n" +
                    "interfacetimeout AS interfaceTimeout,\n" +
                    "requestinterfacetimeout AS requestInterfaceTimeout,\n" +
                    "clientname AS clientName,\n" +
                    "environment AS environment,\n" +
                    "ispracticetest AS isPracticeTest,\n" +
                    "refreshvalue AS refreshValue,\n" +
                    "tainterfacetimeout AS taInterfaceTimeout,\n" +
                    "tacheckintime AS taCheckinTime,\n" +
                    "datechanged AS dateChanged,\n" +
                    "datepublished AS datePublished,\n" +
                    "sessionexpire AS sessionExpiration,\n" +
                    "refreshvaluemultiplier AS refreshValueMultiplier\n" +
                "FROM\n" +
                    "session.timelimits\n" +
                "WHERE\n" +
                    "_efk_testid = :testId\n" +
                    "AND clientname = :clientName\n" +
                "UNION\n" +
                "SELECT\n" +
                    "_efk_testid AS testId,\n" +
                    "oppexpire AS opportunityExpiration,\n" +
                    "opprestart AS opportunityRestart,\n" +
                    "oppdelay AS opportunityDelay,\n" +
                    "interfacetimeout AS interfaceTimeout,\n" +
                    "requestinterfacetimeout AS requestInterfaceTimeout,\n" +
                    "clientname AS clientName,\n" +
                    "environment AS environment,\n" +
                    "ispracticetest AS isPracticeTest,\n" +
                    "refreshvalue AS refreshValue,\n" +
                    "tainterfacetimeout AS taInterfaceTimeout,\n" +
                    "tacheckintime AS taCheckinTime,\n" +
                    "datechanged AS dateChanged,\n" +
                    "datepublished AS datePublished,\n" +
                    "sessionexpire AS sessionExpiration,\n" +
                    "refreshvaluemultiplier AS refreshValueMultiplier\n" +
                "FROM\n" +
                    "session.timelimits\n" +
                "WHERE\n" +
                    "_efk_testid IS NULL\n" +
                    "AND clientname = :clientName";

        try {
            return namedParameterJdbcTemplate.query(
                    SQL,
                    parameters,
                    new BeanPropertyRowMapper<>(TestSessionTimeLimitConfiguration.class));
        } catch (EmptyResultDataAccessException e) {
            logger.warn(String.format("%s did not return any results for testId = %s, clientName = %s", SQL, testId, clientName), e);
            return null;
        }
    }

    public String validateProctorSession(TestSession testSession) {
        return validateProctorSession(testSession, testSession.getProctorId(), testSession.getSessionBrowser());
    }

    /**
     * Makes sure a session is valid
     * Checks
     *  1) Is the session open?
     *  2) Is the session owned by the proctor?
     *  3) Is it being accessed by a proper browser?
     * It returns NULL if everything checks out, or an error message if there is a problem.
     */
    @Override
    public String validateProctorSession(TestSession testSession, Long proctorKey, UUID browserKey) {
        // Emulate logic from CommonDLL.ValidateProctorSession_FN
        //  However, since the TestSession is already loaded we don't need to access the DB again to do this, which saves 3 DB calls

        // Emulate line 1726: SQL_QUERY1
        //  Note: We are not using testSession.isOpen() since the logic here is different than there for some reason
        // Get the current time based on the databases timezone
        Date now = dateUtility.getDbDate();

        if (now.before(testSession.getDateBegin()) || now.after(testSession.getDateEnd())) {
            return "The session is closed.";
        }

        if (testSession.getProctorId() != proctorKey) {
            return "The session is not owned by this proctor";
        }

        if (testSession.getSessionBrowser() != browserKey) {
            return "Unauthorized session access";
        }

        return null;
    }

    /**
     * Pause an existing {@code TestSession}, citing the specified reason.
     * @param session The {@code TestSession} to pause.
     * @param reason A {@code String} describing why the {@code TestSession} was paused.
     */
    @Override
    @Transactional
    public void pause(TestSession session, String reason) {
        final Date closedDate = new Date();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(session.getKey()));
        parameters.put("reason", reason);
        parameters.put("dateChanged", closedDate);
        parameters.put("dateEnd", closedDate);

        final String SQL =
                "UPDATE\n" +
                    "session.session\n" +
                "SET\n" +
                    "status = :reason,\n" +
                    "datechanged = :dateChanged,\n" +
                    "dateend = :dateEnd\n" +
                "WHERE\n" +
                    "_key = :key";

        try {
            namedParameterJdbcTemplate.update(SQL, parameters);
        } catch (DataAccessException e) {
            logger.error(String.format("%s UPDATE threw exception", SQL), e);
            throw e;
        }
    }
}
