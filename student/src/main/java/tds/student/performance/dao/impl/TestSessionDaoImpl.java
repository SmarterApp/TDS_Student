package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.TestSessionDao;
import tds.student.performance.dao.mappers.TestSessionMapper;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.TestSession;
import tds.student.sql.data.Data;

import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            return (TestSession)namedParameterJdbcTemplate.queryForObject(
                    SQL,
                    parameters,
                    new TestSessionMapper());
        } catch(EmptyResultDataAccessException e) {
            logger.warn(String.format("%s did not return any results for key = %s", SQL, key), e);
            return null;
        }
    }

    @Override
    @Transactional
    public Integer getCheckIn(String clientName) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("clientName", clientName);

        final String SQL =
                "SELECT\n" +
                    "tacheckintime AS checkin\n" +
                "FROM\n" +
                    "session.timelimits\n" +
                "WHERE clientname = :clientName\n" +
                    "AND _efk_TestID IS NULL";

        try {
            return namedParameterJdbcTemplate.queryForInt(SQL, parameters);
        } catch (EmptyResultDataAccessException e) {
            logger.warn(String.format("%s did not return any results fro clientName = %s", SQL, clientName), e);
            return null;
        }
    }

    /**
     * Pause an existing {@code TestSession}, citing the specified reason.
     * @param session The {@code TestSession} to pause.
     * @param reason A {@code String} describing why the {@code TestSession} was paused.
     */
    @Override
    @Transactional
    public void pause(TestSession session, String reason) {
        // TODO:  Need to investigate CommonDLL.ValidateProctorSession_FN (line 1684)

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
