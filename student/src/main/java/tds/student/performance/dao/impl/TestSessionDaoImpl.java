package tds.student.performance.dao.impl;

import org.apache.commons.collections.functors.ExceptionClosure;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.TestSessionDao;
import tds.student.performance.dao.mappers.TestSessionMapper;
import tds.student.performance.dao.utils.UuidAdapter;
import tds.student.performance.domain.TestSession;

import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 */
@Repository
public class TestSessionDaoImpl implements TestSessionDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional
    public TestSession get(UUID key) {
        Map<String, byte[]> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(key));

        final String SQL =
                "SELECT\n" +
                    "_key AS `key`,\n" +
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
                    "TACheckInTime AS checkin\n" +
                "FROM\n" +
                    "session.timelimits\n" +
                "WHERE clientname = :clientName\n" +
                    "AND _efk_TestID IS NULL";

        try {
            return namedParameterJdbcTemplate.queryForInt(SQL, parameters);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

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

        namedParameterJdbcTemplate.update(SQL, parameters);
    }
}
