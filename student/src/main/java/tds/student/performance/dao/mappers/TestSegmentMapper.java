package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.domain.TestSegmentItem;
import tds.student.performance.utils.UuidAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jjohnson on 1/2/16.
 */
public class TestSegmentMapper implements RowMapper<TestSegmentItem> {
    @Override
    public TestSegmentItem mapRow(ResultSet resultSet, int i) throws SQLException {
        TestSegmentItem testSegmentItem = new TestSegmentItem();
        testSegmentItem.setOpportunityKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("opportunityKey")));
        testSegmentItem.setSegmentKey(resultSet.getString("segmentKey"));
        testSegmentItem.setSegmentId(resultSet.getString("segmentId"));
        testSegmentItem.setSegmentPosition(resultSet.getInt("segmentPosition"));
        testSegmentItem.setAlgorithm(resultSet.getString("algorithm"));
        testSegmentItem.setOpItemCount(resultSet.getInt("opItemCount"));
        testSegmentItem.setIsPermeable(resultSet.getInt("isPermeable"));
        testSegmentItem.setIsSatisfied(resultSet.getBoolean("isSatisfied"));
        testSegmentItem.setCurrentDate(resultSet.getTimestamp("currentDate"));

        return testSegmentItem;
    }
}
