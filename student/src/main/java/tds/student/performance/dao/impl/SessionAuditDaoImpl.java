package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.SessionAuditDao;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.SessionAudit;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jjohnson on 12/24/15.
 */
@Repository
public class SessionAuditDaoImpl implements SessionAuditDao {
    private static final Logger logger = LoggerFactory.getLogger(SessionAuditDaoImpl.class);
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(SessionAudit sessionAudit) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(sessionAudit.getSessionKey()));
        parameters.put("dateAccessed", sessionAudit.getDateAccessed());
        parameters.put("accessType", sessionAudit.getAccessType());
        parameters.put("hostName", sessionAudit.getHostName());
        parameters.put("browserKey", UuidAdapter.getBytesFromUUID(sessionAudit.getBrowserKey()));
        parameters.put("databaseName", sessionAudit.getDatabaseName());

        final String SQL =
                "INSERT INTO\n" +
                    "archive.sessionaudit (" +
                        "_fk_session," +
                        "dateaccessed," +
                        "accesstype," +
                        "hostname," +
                        "browserkey," +
                        "dbname)\n" +
                    "VALUES(" +
                        ":sessionKey," +
                        ":dateAccessed," +
                        ":accessType," +
                        ":hostName," +
                        ":browserKey," +
                        ":databaseName)";

        try {
            namedParameterJdbcTemplate.update(SQL, parameters);
        } catch (DataAccessException e) {
            logger.error(String.format("%s UPDATE threw exception", SQL), e);
            throw e;
        }
    }
}
