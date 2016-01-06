package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.TestOpportunityAuditDao;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.TestOpportunityAudit;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
* Data Access Object for dealing with {@code TestOpportunityAudit} records.
 */
@Repository
public class TestOpportunityAuditDaoImpl implements TestOpportunityAuditDao {
    private static final Logger logger = LoggerFactory.getLogger(TestOpportunityAuditDaoImpl.class);
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    // TODO: consider renaming to indicate that this may insert many records, not just one.
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
    @Transactional
    public void create(TestOpportunityAudit testOpportunityAudit) {
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
                    "session.testopportunity t\n" +
                "JOIN\n" +
                    "configs.statuscodes s\n" + // This join should alleviate the need to call CommonDLL.GetStatusCodes_FN
                    "ON (s.status = t.status\n" +
                    "AND s.`usage` = 'Opportunity'\n" +
                    "AND s.stage = 'inuse')\n" +
                "WHERE t._fk_session = :sessionKey";

        try {
            namedParameterJdbcTemplate.update(SQL, parameters);
        } catch (DataAccessException e) {
            logger.error(String.format("%s INSERT threw exception", SQL), e);
            throw e;
        }
    }
}
