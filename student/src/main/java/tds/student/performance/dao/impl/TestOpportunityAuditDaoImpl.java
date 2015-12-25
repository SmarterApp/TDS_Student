package tds.student.performance.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.TestOpportunityAuditDao;
import tds.student.performance.dao.utils.UuidAdapter;
import tds.student.performance.domain.TestOpportunityAudit;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jjohnson on 12/24/15.
 */
@Repository
public class TestOpportunityAuditDaoImpl implements TestOpportunityAuditDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    // TODO: consider renaming to indicate that this may insert many records, not just one.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
                        "dataaccessed,\n" +
                        "accesstype,\n" +
                        "_fk_session,\n" +
                        "hostname,\n" +
                        "_fk_browser,\n" +
                        "dbname)\n" +
                "SELECT\n" +
                    "to._key,\n" +
                    ":dateAccessed,\n" +
                    ":accessType,\n" +
                    ":sessionKey,\n" +
                    ":hostName,\n" +
                    "to._fk_Browser,\n" +
                    ":databaseName\n" +
                "FROM\n" +
                    "session.testopportunity to\n" +
                "JOIN\n" +
                    "configs.statuscodes sc\n" + // This join should alleviate the need to call CommonDLL.GetStatusCodes_FN
                    "ON (sc.status = to.status\n" +
                    "AND sc.`usage` = 'Opportunity'\n" +
                    "AND sc.stage = 'inuse')\n" +
                "WHERE to._fk_session = :sessionKey";

        namedParameterJdbcTemplate.update(SQL, parameters);
    }
}
