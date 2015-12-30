package tds.student.performance.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.TesteeResponseDao;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by emunoz on 12/30/15.
 */
@Repository
public class TesteeResponseDaoImpl implements TesteeResponseDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional
    public void updateRestartCount(UUID opportunityKey, Integer restartCount, boolean isRcntSpecific) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("rcnt", restartCount);
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(opportunityKey));

        String SQL =
            "UPDATE\n" +
                "session.testeeresponse\n" +
            "SET\n" +
                "OpportunityRestart = :rcnt + 1\n" +
            "WHERE\n" +
                "_fk_TestOpportunity = :oppKey\n";

        if (isRcntSpecific) {
            SQL += " AND OpportunityRestart = :rcnt";
        }

        namedParameterJdbcTemplate.update(SQL, parameters);
    }
}
