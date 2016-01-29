/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tds.student.performance.dao.TestOpportunityDao;
import tds.student.performance.dao.mappers.TestOpportunityMapper;
import tds.student.performance.domain.TestOpportunityAudit;
import tds.student.performance.utils.LegacyDbNameUtility;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.TestOpportunity;

import javax.sql.DataSource;
import java.sql.Timestamp;
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
    private SimpleJdbcCall resumeItemPositionFunctionCall;

    @Autowired
    private LegacyDbNameUtility dbNameUtility;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.resumeItemPositionFunctionCall = new SimpleJdbcCall(dataSource)
                .withCatalogName(dbNameUtility.getDatabaseName(LegacyDbNameUtility.Databases.Session))
                .withFunctionName("resumeitemposition");
    }

    /**
     * Get a single {@link TestOpportunity} from the {@code session.testopportunity} table for a specified key.
     * <p>
     *     Fetching the environment from {@code session._externs} and the number of records in the
     *     {@code session.sim_segment} tables help us deteremine if this {@code TestOpportunity} is a simulation.
     * </p>
     * <p>
     *     Considered using a {@code CASE WHEN EXISTS (SELECT...)... } for the simulationSegmentCount.  All that really
     *     matters is if the {@code TestOpportunity} has records in the {@code session.sim_segment} table.
     * </p>
     * <p>
     *     The {@code GROUP BY} clause has to be in this query.  If the {@code GROUP BY} is omitted, then the
     *     {@code COUNT} will still return 0 when looking for an opportunity key that does not exist, meaning a row
     *     with all {@code NULL} values and a 0 simulationSegmentCount will be returned instead of no rows (which is the
     *     intent; this method should return {@code null} if no record is found for the specified key).
     * </p>
     *
     * @param key The key for the desired {@link TestOpportunity}.
     * @return {@link TestOpportunity} that corresponds to the specified key.
     */
    @Override
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
                    "o.stage AS stage,\n" +
                    "o.expireFrom AS expireFrom,\n" +
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
                    "${sessiondb}.testopportunity o\n" +
                "LEFT JOIN\n" +
                        "${sessiondb}._externs e\n" +
                        "ON (e.clientname = o.clientname)\n" +
                "LEFT JOIN\n" +
                        "${sessiondb}.sim_segment s\n" +
                        "ON (s._fk_session = o._fk_session\n" +
                        "AND s._efk_adminsubject = o._efk_adminsubject)\n" +
                "WHERE\n" +
                    "_key = :key\n" +
                "GROUP BY _key";

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    dbNameUtility.setDatabaseNames(SQL),
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
                    "${sessiondb}.testopportunity o\n" +
                "JOIN\n" +
                    "${configdb}.statuscodes sc\n" + // This join should alleviate the need to call CommonDLL.GetStatusCodes_FN
                "ON (sc.status = o.status\n" +
                    "AND sc.`usage` = :usage\n" +
                    "AND sc.stage = :stage)\n" +
                "LEFT JOIN\n" +
                    "${sessiondb}._externs e\n" +
                    "ON (e.clientname = o.clientname)\n" +
                "WHERE o._fk_session = :sessionKey";

        return namedParameterJdbcTemplate.query(
                dbNameUtility.setDatabaseNames(SQL),
                parameters,
                new TestOpportunityMapper());
    }

    @Override
    public void update(TestOpportunity opportunity) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("algorithm", opportunity.getAlgorithm());
        parameters.put("browserKey", UuidAdapter.getBytesFromUUID(opportunity.getBrowserKey()));
        parameters.put("clientName", opportunity.getClientName());
        parameters.put("dateChanged", opportunity.getDateChanged());
        parameters.put("dateStarted", opportunity.getDateStarted());
        parameters.put("dateRestarted", opportunity.getDateRestarted());
        parameters.put("environment", opportunity.getEnvironment());
        parameters.put("gracePeriodRestarts", opportunity.getGracePeriodRestarts());
        parameters.put("isSegmented", opportunity.getIsSegmented());
        parameters.put("expireFrom", opportunity.getExpireFrom());
        parameters.put("stage", opportunity.getStage());
        parameters.put("key", UuidAdapter.getBytesFromUUID(opportunity.getKey()));
        parameters.put("maxItems", opportunity.getMaxItems());
        parameters.put("opportunity", opportunity.getOpportunity());
        parameters.put("restartCount", opportunity.getRestartCount());
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(opportunity.getSessionKey()));
        //parameters.put("simulationSegmentCount", opportunity.getSimulationSegmentCount());
        parameters.put("status", opportunity.getStatus());
        parameters.put("subject", opportunity.getSubject());
        parameters.put("testee", opportunity.getTestee());
        parameters.put("testId", opportunity.getTestId());
        parameters.put("testKey", opportunity.getTestKey());
        parameters.put("waitingForSegment", opportunity.getWaitingForSegment());

        final String SQL =
                "UPDATE\n" +
                    "${sessiondb}.testopportunity\n" +
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
                    "gracePeriodRestarts = :gracePeriodRestarts,\n" +
                    "maxItems = :maxItems,\n" +
                    "opportunity = :opportunity,\n" +
                    "subject = :subject,\n" +
                    "clientName = :clientName,\n" +
                    "isSegmented = :isSegmented,\n" +
                    "algorithm = :algorithm,\n" +
                    "environment = :environment,\n" +
                    "stage = :stage,\n" +
                    "expireFrom = :expireFrom,\n" +
                    "waitingForSegment = :waitingForSegment\n" +
                "WHERE\n" +
                    "_key = :key";

        namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(SQL), parameters);
    }

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
                        "${sessiondb}.testopportunity\n" +
                    "WHERE\n" +
                        "_key = :key\n" +
                    "UNION ALL\n" +
                    "SELECT\n" +
                        "MAX(dateSubmitted) AS activityDate\n" +
                    "FROM\n" +
                        "${sessiondb}.testeeresponse\n" +
                    "WHERE\n" +
                        "_fk_TestOpportunity = :key\n" +
                        "AND dateSubmitted IS NOT NULL\n" +
                    "UNION ALL\n" +
                    "SELECT\n" +
                        "MAX(dateGenerated) AS activityDate\n" +
                    "FROM\n" +
                        "${sessiondb}.testeeresponse\n" +
                    "WHERE\n" +
                        "_fk_TestOpportunity = :key\n" +
                        "AND dateGenerated is not null\n" +
                ") as subQuery";

        return namedParameterJdbcTemplate.queryForObject(dbNameUtility.setDatabaseNames(SQL), parameters, Timestamp.class);
    }

    /**
     * Insert a {@code TestOpportunityAudit} record into the {@code archive.opportunityaudit}.
     * <p>
     *     Because the query is an INSERT...SELECT, it is possible that multiple records could be inserted into the
     *     {@code archive.opportunityaudit} table.  The original query (in {@code CommonDLL.GetStatusCodes_FN}) was
     *     written to use an IN clause containing a list of statuses with usage = 'Opportunity' and stage = 'inuse'.
     *     Use of an IN clause could also cause multiple records to be inserted into the {@code archive.opportunityaudit}
     *     table.  Furthermore, the IN clause was built by executing a separate query to get all the status codes then
     *     join them together in a comma- separated list.  The IN clause has been changed to a JOIN, eliminating the
     *     need to make a separate database call to get the status codes.
     * </p>
     * @param testOpportunityAudit The {@code TestOpportunityAudit} that needs to be recorded in the database.
     */
    @Override
    public void createAudit(TestOpportunityAudit testOpportunityAudit) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(testOpportunityAudit.getTestOpportunityKey()));
        parameters.put("dateAccessed", testOpportunityAudit.getDateAccessed());
        parameters.put("accessType", testOpportunityAudit.getAccessType());
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(testOpportunityAudit.getSessionKey()));
        parameters.put("hostName", testOpportunityAudit.getHostName());
        parameters.put("databaseName", testOpportunityAudit.getDatabaseName());

        final String SQL =
                "INSERT INTO\n" +
                    "archive.opportunityaudit (\n" +
                    "_fk_testopportunity,\n" +
                    "dateaccessed,\n" +
                    "accesstype,\n" +
                    "_fk_session,\n" +
                    "hostname,\n" +
                    "_fk_browser,\n" +
                    "dbname)\n" +
                "SELECT\n" +
                    "t._key,\n" +
                    ":dateAccessed,\n" +
                    ":accessType,\n" +
                    ":sessionKey,\n" +
                    ":hostName,\n" +
                    "t._fk_Browser,\n" +
                    ":databaseName\n" +
                "FROM\n" +
                    "${sessiondb}.testopportunity t\n" +
                "JOIN\n" +
                    "${configdb}.statuscodes s\n" + // This join should alleviate the need to call CommonDLL.GetStatusCodes_FN
                    "ON (s.status = t.status\n" +
                    "AND s.`usage` = 'Opportunity'\n" +
                    "AND s.stage = 'inuse')\n" +
                "WHERE t._fk_session = :sessionKey";

        try {
            namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(SQL), parameters);
        } catch (DataAccessException e) {
            logger.error(String.format("%s INSERT threw exception", SQL), e);
            throw e;
        }
    }

    /**
     * Determine the position when a student restarts/resumes an existing {@code TestOpportunity}.
     * <p>
     *     This method emulates {@code StudentDLL.ResumeItemPosition_FN}.  Rather than try to refactor all the SQL
     *     contained within the legacy method, this method invokes the {@code session.resumeitemposition} function on
     *     the database.
     * </p>
     *
     * @param opportunityKey The id of the {@code TestOpportunity}.
     * @param newRestartInstance The next restart of the  {@code TestOpportunity}.
     * @return An integer representing the item where the restarted test should begin.  This value is for
     * {@code TestConfig.getStartPosition()}.
     */
    @Override
    public Integer getResumeItemPosition(UUID opportunityKey, Integer newRestartInstance) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("v_oppkey", UuidAdapter.getBytesFromUUID(opportunityKey))
                .addValue("v_restart", newRestartInstance);

        return resumeItemPositionFunctionCall.executeFunction(Integer.class, parameterSource);
    }
}
