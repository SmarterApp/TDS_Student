package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.TestOpportunity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
* Map a database row from {@code session.testopportunity} to a {@code TestOpportunity} object.
 * <p>
 *     <strong>NOTE:</strong> this is by no means an exhaustive list of fields for the {@code session.testopportunity}.  This
 *     mapper only contains enough fields to facilitate the {@code startTestOpportunity} method.
 * </p>
 * <p>
 *     NOTE:  Using {@code getObject} for the primitive types because calling {@code getLong()} or
 *     {@code getInt()} will set the property to 0 when the value is null in the database.  There are
 *     many sections of code that check for null Integers (among others).  If the Integer was set to 0 instead
 *     of null, unexpected behavior could be introduced.
 *
 *     This only applies for custom mappers; using the {@code BeanRowPropertyMapper} does the correct
 *     behavior (that is, an Integer can be set to null if that's what it is in the database).
 * </p>
 *
 */
public class TestOpportunityMapper implements RowMapper<TestOpportunity> {
    @Override
    public TestOpportunity mapRow(ResultSet resultSet, int i) throws SQLException {
        TestOpportunity testOpportunity = new TestOpportunity();
        testOpportunity.setKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("key")));
        testOpportunity.setSessionKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("sessionKey")));
        testOpportunity.setBrowserKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("browserKey")));
        testOpportunity.setTestKey(resultSet.getString("testKey"));
        testOpportunity.setTestee((Long)resultSet.getObject("testee"));
        testOpportunity.setTestId(resultSet.getString("testId"));
        testOpportunity.setOpportunity((Integer)resultSet.getObject("opportunity"));
        testOpportunity.setStatus(resultSet.getString("status"));
        testOpportunity.setDateStarted(resultSet.getTimestamp("dateStarted"));
        testOpportunity.setDateChanged(resultSet.getTimestamp("dateChanged"));
        testOpportunity.setRestartCount((Integer)resultSet.getObject("rcnt"));
        testOpportunity.setGracePeriodRestarts((Integer)resultSet.getObject("gpRestarts"));
        testOpportunity.setMaxItems((Integer)resultSet.getObject("testLength"));
        testOpportunity.setSubject(resultSet.getString("subject"));
        testOpportunity.setClientName(resultSet.getString("clientName"));
        testOpportunity.setIsSegmented((Boolean)resultSet.getObject("issegmented"));
        testOpportunity.setAlgorithm(resultSet.getString("algorithm"));
        testOpportunity.setEnvironment(resultSet.getString("environment"));
        testOpportunity.setSimulationSegmentCount((Long)resultSet.getObject("simulationSegmentCount"));

        return testOpportunity;
    }
}
