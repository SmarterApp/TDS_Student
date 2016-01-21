package tds.student.performance.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.student.performance.dao.TestSegmentDao;
import tds.student.performance.utils.UuidAdapter;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jjohnson on 1/2/16.
 */
@Repository
public class TestSegmentDaoImpl implements TestSegmentDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Gets the test length by querying the testopportunitysegment table.
     *
     * @param oppKey the opportunity key of the segment length to obtain.
     * @return
     */
    @Override
    public Integer getTestLengthForOpportunitySegment(UUID oppKey) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));

        final String SQL =
            "SELECT\n" +
                "CONVERT(SUM(opItemCnt), SIGNED) + CONVERT(SUM(ftItemCnt), SIGNED) AS testLength\n" +
            "FROM\n" +
                "session.testopportunitysegment\n" +
            "WHERE\n" +
                "_fk_TestOpportunity = :oppKey";

        return namedParameterJdbcTemplate.queryForInt(SQL, parameters);
    }
}
