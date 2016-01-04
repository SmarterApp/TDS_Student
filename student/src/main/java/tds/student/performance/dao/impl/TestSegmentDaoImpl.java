package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.caching.CacheType;
import tds.student.performance.dao.TestSegmentDao;
import tds.student.performance.dao.mappers.TestSegmentMapper;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSegmentItem;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jjohnson on 1/2/16.
 */
@Repository
public class TestSegmentDaoImpl implements TestSegmentDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationDaoImpl.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Get a collection of {@link TestSegmentItem}s for a {@link TestOpportunity} marked as a
     * simulation.
     *
     * @param testOpportunity The simulation {@code TestOpportunity}.
     * @return A {@link TestSegmentItem} collection.
     */
    @Override
    @Transactional
    public List<TestSegmentItem> getForSimulation(TestOpportunity testOpportunity) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", testOpportunity.getKey());
        parameters.put("sessionKey", testOpportunity.getSessionKey());
        parameters.put("testKey", testOpportunity.getTestKey());
        parameters.put("isPermeable", -1);
        parameters.put("isSatisfied", false);

        final String SQL =
                "SELECT\n" +
                    ":key AS opportunityKey,\n" +
                    "_efk_segment AS segmentKey,\n" +
                    "segmentid AS segmentId,\n" +
                    "segmentposition AS segmentPosition,\n" +
                    "selectionalgorithm AS algorithm,\n" +
                    "maxitems AS opItemCount,\n" +
                    ":isPermeable AS isPermeable,\n" +
                    ":isSatisfied AS isSatisfied,\n" +
                    "NOW(3) AS currentDate\n" +
                "FROM\n" +
                    "session.sim_segment\n" +
                "WHERE\n" +
                    "_fk_session = :sessionKey\n" +
                    "AND _efk_adminsubject = :testKey";

        return namedParameterJdbcTemplate.query(
                SQL,
                parameters,
                new TestSegmentMapper());
    }

    /**
     * Get a collection of {@link TestSegmentItem}s for a {@link TestOpportunity} marked as
     * segmented.
     *
     * @param testOpportunity The segmented {@code TestOpportunity}.
     * @return A {@link TestSegmentItem} collection.
     */
    @Override
    @Transactional
    @Cacheable(CacheType.LongTerm)
    public List<TestSegmentItem> getSegmented(TestOpportunity testOpportunity) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", testOpportunity.getKey());
        parameters.put("testKey", testOpportunity.getTestKey());
        parameters.put("isPermeable", -1);
        parameters.put("isSatisfied", false);

        final String SQL =
                "SELECT\n" +
                    ":key AS opportunityKey,\n" +
                    "_key AS segmentKey,\n" +
                    "testid AS segmentId,\n" +
                    "testposition AS segmentPosition,\n" +
                    "selectionalgorithm AS algorithm,\n" +
                    "maxitems AS opItemCount,\n" +
                    ":isPermeable AS isPermeable,\n" +
                    ":isSatisfied AS isSatisfied,\n" +
                    "NOW(3) AS currentDate\n" +
                "FROM\n" +
                    "itembank.tblsetofadminsubjects\n" +
                "WHERE\n" +
                    "virtualtest = :testKey";

        return namedParameterJdbcTemplate.query(
                SQL,
                parameters,
                new TestSegmentMapper());
    }

    /**
     * Get a {@link TestSegmentItem} collection for a {@link TestOpportunity} that is not segmented, thus making the
     * test its own segment.
     * <p>
     *     Since the query for this method uses the primary key in the WHERE clause, only one record will ever be
     *     returned.  This method returns a {@code List<TestSegmentItem>} anyway to keep the signature in linen with the
     *     other possible ways of getting the {@code TestSegmentItem}s for the {@code TestOpportunity}.
     * </p>
     *
     * @param testOpportunity The {@code TestOpportunity} that is not segmented.
     * @return A {@link TestSegmentItem} collection for the {@code TestOpportunity.getTestKey()}.
     */
    @Override
    @Transactional
    @Cacheable(CacheType.LongTerm)
    public List<TestSegmentItem> get(TestOpportunity testOpportunity) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", UuidAdapter.getBytesFromUUID(testOpportunity.getKey()));
        parameters.put("testKey", testOpportunity.getTestKey());
        parameters.put("isPermeable", -1);
        parameters.put("isSatisfied", false);

        final String SQL =
            "SELECT\n" +
                ":key AS opportunityKey,\n" +
                "_key AS segmentKey,\n" +
                "testid AS segmentId,\n" +
                "1 AS segmentPosition,\n" +
                "selectionalgorithm AS algorithm,\n" +
                "maxitems AS opItemCount,\n" +
                ":isPermeable AS isPermeable,\n" +
                ":isSatisfied AS isSatisfied,\n" +
                "NOW(3) AS currentDate\n" +
            "FROM\n" +
                "itembank.tblsetofadminsubjects\n" +
            "WHERE\n" +
                " _key = :testKey";

        return namedParameterJdbcTemplate.query(
                SQL,
                parameters,
                new TestSegmentMapper());
    }

    /**
     * Gets the test length by querying the testopportunitysegment table.
     *
     * @param oppKey the opportunity key of the segment length to obtain.
     * @return
     */
    @Override
    @Transactional
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

    @Override
    @Transactional
    public void createTestOpportunitySegments(TestOpportunity testOpportunity, List<TestSegmentItem> items) {

    }
}
