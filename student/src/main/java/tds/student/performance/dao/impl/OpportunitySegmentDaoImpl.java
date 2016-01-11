package tds.student.performance.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.OpportunitySegmentDao;
import tds.student.performance.domain.OpportunitySegment;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Data Access Object to query testopportunity and  testopportunitysegment
 */
@Repository
public class OpportunitySegmentDaoImpl implements OpportunitySegmentDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    //private static final Logger logger = LoggerFactory.getLogger(OpportunitySegmentDaoImpl.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional
    public OpportunitySegment getOpportunitySegmentAccommodation(UUID oppKey, Integer segment) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));
        parameters.put("segment", segment);


        final String SQL = "SELECT \n" +
                "    o.clientname AS clientName,        \n" +
                "    o._efk_adminsubject AS testKey,    \n" +
                "    o.restart AS restart,              \n" +
                "    o.status,                          \n" +
                "    o.environment,                     \n" +
                "    o._efk_testee AS testee,\n" +
                "    s._efk_segment AS segmentKey,\n" +
                "    s.formkey AS formKey,              \n" +
                "    s.algorithm,                       \n" +
                "    a.acccode AS language        \n" +
                "FROM\n" +
                "    testopportunity o\n" +
                "        LEFT OUTER JOIN\n" +
                "    testopportunitysegment s ON (s._fk_testopportunity = o._key AND s.segmentposition = :segment)\n" +
                "        LEFT OUTER JOIN\n" +
                "    testeeaccommodations a ON (a._fk_testopportunity = o._key AND a.acctype = 'Language')\n" +
                "WHERE\n" +
                "    _key = :oppKey";

        return namedParameterJdbcTemplate.queryForObject(
                SQL,
                parameters,
                new BeanPropertyRowMapper<>(OpportunitySegment.class));
    }

}
