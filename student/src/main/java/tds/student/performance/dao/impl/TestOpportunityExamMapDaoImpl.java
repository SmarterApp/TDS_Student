package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import tds.dll.common.performance.utils.LegacyDbNameUtility;
import tds.student.performance.dao.TestOpportunityExamMapDao;

@Repository
public class TestOpportunityExamMapDaoImpl implements TestOpportunityExamMapDao {
    private static final Logger logger = LoggerFactory.getLogger(TestOpportunityExamMapDaoImpl.class);
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private LegacyDbNameUtility dbNameUtility;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void insert(final UUID testOpportunityId, final UUID examId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("testopportunityId", testOpportunityId.toString());
        parameters.put("examId", examId.toString());

        final String sql = "INSERT INTO ${sessiondb}.testopportunity_exam_map (testopportunity_id, exam_id) " +
            "VALUES (:testopportunityId, :examId)";

        try {
            namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(sql), parameters);
        } catch (DataAccessException e) {
            logger.error(String.format("%s UPDATE threw exception", sql), e);
        }
    }
}
