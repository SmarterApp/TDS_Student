package tds.student.performance.dao.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.TestOpportunityDao;
import tds.student.performance.dao.mappers.TestOpportunityMapper;
import tds.student.performance.dao.utils.UuidAdapter;
import tds.student.performance.domain.TestOpportunity;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
Data access object for accessing records in the {@code session.testopportunity} table.
 */
@Repository
public class TestOpportunityDaoImpl implements TestOpportunityDao {
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
                    "_key AS `key`,\n" +
                    "_fk_Session AS sessionKey,\n" +
                    "_fk_browser AS browserKey,\n" +
                    "_efk_AdminSubject AS testKey,\n" +
                    "_efk_Testee AS testee,\n" +
                    "_efk_TestID AS testId,\n" +
                    "_efk_AdminSubject AS test,\n" +
                    "opportunity AS opportunity,\n" +
                    "status AS status,\n" +
                    "DateStarted AS dateStarted,\n" +
                    "DateChanged AS dateChanged,\n" +
                    "Restart AS rcnt,\n" +
                    "GracePeriodRestarts AS gpRestarts,\n" +
                    "maxitems AS testLength,\n" +
                    "Subject AS subject,\n" +
                    "clientname AS clientName\n" +
                "FROM\n" +
                    "session.testopportunity\n" +
                "WHERE\n" +
                    "_key = :key";

        try {
            return (TestOpportunity) namedParameterJdbcTemplate.queryForObject(
                    SQL,
                    parameters,
                    new TestOpportunityMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
