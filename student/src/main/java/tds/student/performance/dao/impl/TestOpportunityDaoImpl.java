package tds.student.performance.dao.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.dll.api.ICommonDLL;
import tds.student.performance.dao.TestOpportunityDao;
import tds.student.performance.dao.mappers.TestOpportunityMapper;
import tds.student.performance.domain.ClientSystemFlag;
import tds.student.performance.utils.LegacySqlConnection;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.TestOpportunity;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
Data access object for accessing records in the {@code session.testopportunity} table.
 */
@Repository
public class TestOpportunityDaoImpl implements TestOpportunityDao {
    private static final Logger logger = LoggerFactory.getLogger(TestOpportunityDaoImpl.class);
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Get a single {@code TestOpportunity} from the {@code session.testopportunity} table for a specified key.
     * @param key The key for the desired {@code TestOpportunity}.
     * @return (@code TestOpportunity} that corresponds to the specified key.
     * @throws ReturnStatusException
     */
    @Override
    @Transactional
    public TestOpportunity get(UUID key) {
        Map<String, byte[]> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(key));

        final String SQL =
                "SELECT\n" +
                    "o._key AS `key`,\n" +
                    "o._fk_session AS sessionKey,\n" +
                    "o._fk_browser AS browserKey,\n" +
                    "o._efk_adminsubject AS testKey,\n" +
                    "o._efk_testee AS testee,\n" +
                    "o._efk_testid AS testId,\n" +
                    "o._efk_adminsubject AS test,\n" +
                    "o.opportunity AS opportunity,\n" +
                    "o.status AS status,\n" +
                    "o.datestarted AS dateStarted,\n" +
                    "o.datechanged AS dateChanged,\n" +
                    "o.restart AS rcnt,\n" +
                    "o.graceperiodrestarts AS gpRestarts,\n" +
                    "o.maxitems AS testLength,\n" +
                    "o.subject AS subject,\n" +
                    "o.clientname AS clientName,\n" +
                    "o.issegmented AS isSegmented,\n" +
                    "o.algorithm AS algorithm,\n" +
                    "e.environment AS environment,\n" +
                    "COUNT(s._fk_session) AS simulationSegmentCount\n" +
                "FROM\n" +
                    "session.testopportunity o\n" +
                "LEFT JOIN\n" +
                        "session._externs e\n" +
                        "ON (e.clientname = o.clientname)\n" +
                "LEFT JOIN\n" +
                        "session.sim_segment s\n" +
                        "ON (o._fk_session = s._fk_session\n" +
                        "AND o._efk_adminsubject = o._efk_adminsubject)\n" +
                "WHERE\n" +
                    "_key = :key\n" +
                "GROUP BY \n" +
                    "o._key,\n" +
                    "o._fk_session,\n" +
                    "o._fk_browser,\n" +
                    "o._efk_adminsubject,\n" +
                    "o._efk_testee ,\n" +
                    "o._efk_testid ,\n" +
                    "o._efk_adminsubject,\n" +
                    "o.opportunity,\n" +
                    "o.status,\n" +
                    "o.datestarted,\n" +
                    "o.datechanged,\n" +
                    "o.restart,\n" +
                    "o.graceperiodrestarts ,\n" +
                    "o.maxitems,\n" +
                    "o.subject ,\n" +
                    "o.clientname ,\n" +
                    "o.issegmented ,\n" +
                    "o.algorithm ,\n" +
                    "e.environment \n" +
                "HAVING COUNT(s._fk_session) != 0";

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    SQL,
                    parameters,
                    new TestOpportunityMapper());
        } catch (EmptyResultDataAccessException e) {
            logger.warn(String.format("%s did not return results for ky = %s", SQL, key));
            return null;
        }

    }

    @Override
    public List<TestOpportunity> getBySessionAndStatus(UUID sessionKey, String statusUsage, String statusStage) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(sessionKey));
        parameters.put("usage", statusUsage);
        parameters.put("stage", statusStage);

        final String SQL =
                "SELECT\n" +
                    "o._key AS `key`,\n" +
                    "o._fk_session AS sessionKey,\n" +
                    "o._fk_browser AS browserKey,\n" +
                    "o._efk_adminsubject AS testKey,\n" +
                    "o._efk_testee AS testee,\n" +
                    "o._efk_testid AS testId,\n" +
                    "o._efk_adminsubject AS test,\n" +
                    "o.opportunity AS opportunity,\n" +
                    "o.status AS status,\n" +
                    "o.datestarted AS dateStarted,\n" +
                    "o.datechanged AS dateChanged,\n" +
                    "o.restart AS rcnt,\n" +
                    "o.graceperiodrestarts AS gpRestarts,\n" +
                    "o.maxitems AS testLength,\n" +
                    "o.subject AS subject,\n" +
                    "o.clientname AS clientName,\n" +
                    "o.issegmented AS isSegmented,\n" +
                    "o.algorithm AS algorithm,\n" +
                    "e.environment AS environment,\n" +
                    "0 AS simulationSegmentCount\n" + // TODO: this is not used right now so not adding the extra LEFT JOIN at this point
                "FROM\n" +
                    "session.testopportunity o\n" +
                "JOIN\n" +
                    "configs.statuscodes sc\n" + // This join should alleviate the need to call CommonDLL.GetStatusCodes_FN
                "ON (sc.status = o.status\n" +
                    "AND sc.`usage` = :usage\n" +
                    "AND sc.stage = :stage)\n" +
                "LEFT JOIN\n" +
                    "session._externs e\n" +
                    "ON (e.clientname = o.clientname)\n" +
                "WHERE o._fk_session = :sessionKey";

        return namedParameterJdbcTemplate.query(
                SQL,
                parameters,
                new TestOpportunityMapper());
    }
}
