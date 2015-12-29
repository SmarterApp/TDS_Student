package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.student.performance.dao.DbLatencyDao;
import tds.student.performance.utils.HostNameHelper;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.*;

/**
 * Data Access Object for interacting with the {@code _dblatency} database.
 * <p>
 *     This class is called from the {@code DbLatencyService} to insert a new record into the archive._dblatency table.
 *     It can be disabled in production by setting the logLatency.enabled property to false.  This method won't get called then through the service.
 * </p>
 */
@Repository
public class DbLatencyDaoImpl implements DbLatencyDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(DbLatencyDaoImpl.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public void create(String procName, Long duration, Date startTime, Date diffTime, Long userKey, Integer n, UUID testoppKey, UUID sessionKey, String clientName, String comment) {
        final String sessionDb = "session";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userKey", userKey);
        parameters.put("duration", duration);
        parameters.put("startTime", startTime);
        parameters.put("diffTime", diffTime);
        parameters.put("procName", procName);
        parameters.put("N", n);
        parameters.put("testoppKey", UuidAdapter.getBytesFromUUID(testoppKey));
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(sessionKey));
        parameters.put("clientName", clientName);
        parameters.put("comment", comment);
        parameters.put("localhost", HostNameHelper.getHostName());
        parameters.put("dbName", sessionDb);

        final String SQL =
                "INSERT INTO\n" +
                        "archive._dblatency (" +
                        "userkey," +
                        "duration," +
                        "starttime," +
                        "difftime," +
                        "procname," +
                        "N," +
                        "_fk_TestOpportunity," +
                        "_fk_session," +
                        "clientname," +
                        "comment," +
                        "host," +
                        "dbname)\n" +
                        "VALUES(" +
                        ":userKey," +
                        ":duration," +
                        ":startTime," +
                        ":diffTime," +
                        ":procName," +
                        ":N," +
                        ":testoppKey," +
                        ":sessionKey," +
                        ":clientName," +
                        ":comment," +
                        ":localhost," +
                        ":dbName)";

        try {
            namedParameterJdbcTemplate.update(SQL, parameters);
        } catch (DataAccessException e) {
            logger.error(String.format("%s INSERT threw exception", SQL), e);
            throw e;
        }
    }

}
