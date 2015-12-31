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
import java.sql.Timestamp;
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
     * Get a single {@link TestOpportunity} from the {@code session.testopportunity} table for a specified key.
     *
     * @param key The key for the desired {@link TestOpportunity}.
     * @return {@link TestOpportunity} that corresponds to the specified key.
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
                    "o.opportunity AS opportunity,\n" +
                    "o.status AS status,\n" +
                    "o.datestarted AS dateStarted,\n" +
                    "o.datechanged AS dateChanged,\n" +
                    "o.daterestarted AS dateRestarted,\n" +
                    "o.restart AS rcnt,\n" +
                    "o.graceperiodrestarts AS gpRestarts,\n" +
                    "o.maxitems AS testLength,\n" +
                    "o.subject AS subject,\n" +
                    "o.clientname AS clientName,\n" +
                    "o.issegmented AS isSegmented,\n" +
                    "o.algorithm AS algorithm,\n" +
                    "o.waitingForSegment AS waitingForSegment,\n" +
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
                "GROUP BY\n" +
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
                    "e.environment \n";

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

    @Override
    @Transactional
    public void update(TestOpportunity opportunity) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("algorithm", opportunity.getAlgorithm());
        parameters.put("browserKey", opportunity.getBrowserKey());
        parameters.put("clientName", opportunity.getClientName());
        parameters.put("dateChanged", opportunity.getDateChanged());
        parameters.put("dateStarted", opportunity.getDateStarted());
        parameters.put("dateRestarted", opportunity.getDateRestarted());
        parameters.put("environment", opportunity.getEnvironment());
        parameters.put("gracePeriodRestarts", opportunity.getGracePeriodRestarts());
        parameters.put("isSegmented", opportunity.getIsSegmented());
        parameters.put("key", opportunity.getKey());
        parameters.put("maxItems", opportunity.getMaxItems());
        parameters.put("opportunity", opportunity.getOpportunity());
        parameters.put("restartCount", opportunity.getRestartCount());
        parameters.put("sessionKey", opportunity.getSessionKey());
        //parameters.put("simulationSegmentCount", opportunity.getSimulationSegmentCount());
        parameters.put("status", opportunity.getStatus());
        parameters.put("subject", opportunity.getSubject());
        parameters.put("testee", opportunity.getTestee());
        parameters.put("testId", opportunity.getTestId());
        parameters.put("testKey", opportunity.getTestKey());
        parameters.put("waitingForSegment", opportunity.getWaitingForSegment());

        final String SQL =
                "UPDATE\n" +
                    "session.testopportunity\n" +
                "SET\n" +
                    "_fk_session = :sessionKey,\n" +
                    "_fk_browser = :browserKey,\n" +
                    "_efk_adminsubject = :testKey,\n" +
                    "_efk_testee = :testee,\n" +
                    "_efk_testid = :testId,\n" +
                    "prevStatus = status,\n" +
                    "status = :status,\n" +
                    "restart = :restartCount,\n" +
                    "dateChanged = :dateChanged,\n" +
                    "dateStarted = :dateStarted,\n" +
                    "dateRestarted = :dateRestarted,\n" +
                    "gracePeriodRestarts = :gradePeriodRestarts,\n" +
                    "maxItems = :maxItems,\n" +
                    "opportunity = :opportunity,\n" +
                    "subject = :subject,\n" +
                    "clientName = :clientName,\n" +
                    "isSegmented = :isSegmented,\n" +
                    "algorithm = :algorithm,\n" +
                    "environment = :environment,\n" +
                    "waitingForSegment = :waitingForSegment\n" +
                "WHERE\n" +
                    "key = :key";

        namedParameterJdbcTemplate.update(SQL, parameters);
    }

    @Transactional
    @Override
    public Timestamp getLastActivity(UUID key) {
        Map<String, byte[]> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(key));

        final String SQL =
                "SELECT\n" +
                    "MAX(activityDate)\n" +
                "FROM (\n" +
                    "SELECT\n" +
                        "datePaused AS activityDate\n" +
                    "FROM\n" +
                        "session.testopportunity\n" +
                    "WHERE\n" +
                        "_key = :key\n" +
                    "UNION ALL\n" +
                    "SELECT\n" +
                        "MAX(dateSubmitted) AS activityDate\n" +
                    "FROM\n" +
                        "session.testeeresponse\n" +
                    "WHERE\n" +
                        "_fk_TestOpportunity = :key\n" +
                        "AND dateSubmitted IS NOT NULL\n" +
                    "UNION ALL\n" +
                    "SELECT\n" +
                        "MAX(dateGenerated) AS activityDate\n" +
                    "FROM\n" +
                        "session.testeeresponse\n" +
                    "WHERE\n" +
                        "_fk_TestOpportunity = :key\n" +
                        "AND dateGenerated is not null\n" +
                ") as subQuery";

        return namedParameterJdbcTemplate.queryForObject(SQL, parameters, Timestamp.class);
    }
}
