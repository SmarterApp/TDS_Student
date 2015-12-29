package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.TestOpportunity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
Map a database row from {@code session.testopportunity} to a {@code TestOpportunity} object.
 <p>
 <strong>NOTE:</strong> this is by no means an exhaustive list of fields for the {@code session.testopportunity}.  This
 mapper only contains enough fields to facilitate the {@code startTestOpportunity} method.
 </p>
 */
public class TestOpportunityMapper implements RowMapper<TestOpportunity> {
    @Override
    public TestOpportunity mapRow(ResultSet resultSet, int i) throws SQLException {
        TestOpportunity testOpportunity = new TestOpportunity();
        testOpportunity.setKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("key")));
        testOpportunity.setSessionKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("sessionKey")));
        testOpportunity.setBrowserKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("browserKey")));
        testOpportunity.setTestKey(resultSet.getString("testKey"));
        testOpportunity.setTestee(resultSet.getDouble("testee"));
        testOpportunity.setTestId(resultSet.getString("testId"));
        testOpportunity.setAdminSubject(resultSet.getString("test"));
        testOpportunity.setOpportunity(resultSet.getInt("opportunity"));
        testOpportunity.setStatus(resultSet.getString("status"));
        testOpportunity.setDateStarted(resultSet.getTimestamp("dateStarted"));
        testOpportunity.setDateChanged(resultSet.getTimestamp("dateChanged"));
        testOpportunity.setRestartCount(resultSet.getInt("rcnt"));
        testOpportunity.setGracePeriodRestarts(resultSet.getInt("gpRestarts"));
        testOpportunity.setMaxItems(resultSet.getInt("testLength"));
        testOpportunity.setSubject(resultSet.getString("subject"));
        testOpportunity.setClientName(resultSet.getString("clientName"));
        testOpportunity.setIsSegmented(resultSet.getBoolean("issegmented"));
        testOpportunity.setAlgorithm(resultSet.getString("algorithm"));
        testOpportunity.setEnvironment(resultSet.getString("environment"));
        testOpportunity.setSimulationSegmentCount(resultSet.getInt("simulationSegmentCount"));

        return testOpportunity;
    }
}
