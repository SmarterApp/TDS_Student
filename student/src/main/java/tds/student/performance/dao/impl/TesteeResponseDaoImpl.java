package tds.student.performance.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.TesteeResponseDao;
import tds.student.performance.domain.UnfinishedResponsePage;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
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

    @Override
    public List<UnfinishedResponsePage> getUnfinishedPages(UUID opportunityKey) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(opportunityKey));

        final String SQL =
            "SELECT\n" +
                "'false' as isVisible,\n" +
                "page,\n" +
                "groupItemsRequired as groupRequired,\n" +
                "count(*) as numItems,\n" +
                "sum(convert(isValid, SIGNED)) as validCount,\n" +
                "sum(convert(isRequired, SIGNED)) as requiredItems,\n" +
                "sum(\n" +
                    "CASE WHEN isRequired = 1 AND isValid = 1 THEN 1 ELSE 0 END\n" +
                ") as requiredResponses\n" +
            "FROM\n" +
                "session.testeeresponse\n" +
            "WHERE\n" +
                "_fk_TestOpportunity = :oppKey AND\n" +
                "DateGenerated is not null\n" +
            "GROUP BY\n" +
                "page, groupItemsRequired";

        return namedParameterJdbcTemplate.query(SQL, parameters,
                new BeanPropertyRowMapper<>(UnfinishedResponsePage.class));
    }
}
