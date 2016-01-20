package tds.student.performance.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.TesteeResponseDao;
import tds.student.performance.domain.UnfinishedResponsePage;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

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

    /**
     * Update the {@code session.testeeresponse} table to display the pages that should be visible to a student that is
     * restarting a test.
     * <p>
     *     This method emulates functionality of {@code StudentDLL._UnfinishedResponsePages_SP} @ line 5146
     * </p>
     *
     * @param opportunityKey The id of the {@code TestOpportunity} being restarted.
     * @param pageIds The ids of the page(s) that should be visible.
     * @param newRestart The next sequence in the number of restarts for this {@code TestOpportunity}
     */
    @Override
    public void updateRestartCountForPages(UUID opportunityKey, List<Integer> pageIds, Integer newRestart) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pages", pageIds);
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(opportunityKey));
        parameters.put("newRestart", newRestart);

        final String SQL =
                "UPDATE\n" +
                    "session.testeeresponse\n" +
                "SET\n" +
                    "opportunityrestart = :newRestart\n" +
                "WHERE\n" +
                    "_fk_testopportunity = :oppKey\n" +
                    "AND page IN (:pages)";

        namedParameterJdbcTemplate.update(SQL, parameters);
    }

    @Override
    public Long getTesteeResponseItemCount(UUID oppKey) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));

        final String SQL =
            "SELECT\n" +
                "COUNT(*) as numItems\n" +
            "FROM\n" +
                "session.testeeresponse\n" +
            "WHERE\n" +
                "_fk_TestOpportunity = :oppKey";

        return namedParameterJdbcTemplate.queryForLong(SQL, parameters);
    }

    @Override
    public List<UnfinishedResponsePage> getUnfinishedPages(UUID oppKey) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));

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


    @Override
    public void insertBatch(UUID oppKey, final Integer maxPosition) {
        List<SqlParameterSource> parameters = new ArrayList<>();
        for (int pos = 1; pos <= maxPosition; ++pos) {
            parameters.add(new MapSqlParameterSource()
                    .addValue("oppKey", UuidAdapter.getBytesFromUUID(oppKey))
                    .addValue("position", pos));
        }

        final String SQL =
                "INSERT INTO\n" +
                    "session.testeeresponse\n" +
                "(_fk_TestOpportunity, position)\n" +
                "VALUES(:oppKey, :position)";

        namedParameterJdbcTemplate.batchUpdate(SQL, parameters.toArray(new SqlParameterSource[0]));
    }

    @Override
    public void delete(UUID oppKey) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));

        final String SQL =
            "DELETE FROM\n" +
                "session.testeeresponse\n" +
            "WHERE\n" +
                "_fk_TestOpportunity = :oppKey";

        namedParameterJdbcTemplate.update(SQL, parameters);
    }
}
