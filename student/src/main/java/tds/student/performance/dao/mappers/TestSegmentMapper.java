package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.domain.TestSegmentItem;
import tds.student.performance.utils.UuidAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * NOTE:  Using {@code getObject} for the primitive types because calling {@code getLong()} or
 * {@code getInt()} will set the property to 0 when the value is null in the database.  There are
 * many sections of code that check for null Integers (among others).  If the Integer was set to 0 instead
 * of null, unexpected behavior could be introduced.
 *
 * This only applies for custom mappers; using the {@code BeanRowPropertyMapper} does the correct
 * behavior (that is, an Integer can be set to null if that's what it is in the database).
 */
public class TestSegmentMapper implements RowMapper<TestSegmentItem> {
    @Override
    public TestSegmentItem mapRow(ResultSet resultSet, int i) throws SQLException {
        TestSegmentItem testSegmentItem = new TestSegmentItem();
        testSegmentItem.setOpportunityKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("opportunityKey")));
        testSegmentItem.setSegmentKey(resultSet.getString("segmentKey"));
        testSegmentItem.setSegmentId(resultSet.getString("segmentId"));
        testSegmentItem.setSegmentPosition((Integer)resultSet.getObject("segmentPosition", Integer.class));
        testSegmentItem.setAlgorithm(resultSet.getString("algorithm"));
        testSegmentItem.setOpItemCount((Integer)resultSet.getObject("opItemCount"));
        testSegmentItem.setIsPermeable((Integer)resultSet.getObject("isPermeable", Integer.class));
        testSegmentItem.setIsSatisfied((Boolean)resultSet.getObject("isSatisfied", Boolean.class));
        testSegmentItem.setCurrentDate(resultSet.getTimestamp("currentDate"));

        return testSegmentItem;
    }
}
