package tds.student.performance.dao.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.TestOpportunityDao;
import tds.student.performance.dao.mappers.TestOpportunityMapper;
import tds.student.performance.utils.UuidAdapter;
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
                    "_key = :key";

        return namedParameterJdbcTemplate.queryForObject(
                SQL,
                parameters,
                new TestOpportunityMapper());
    }
}
