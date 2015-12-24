package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.dao.utils.UuidAdapter;
import tds.student.performance.domain.TestOpportunity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
Map a database row from {@code session.testopportunity} to a {@code TestOpportunity}.
 */
public class TestOpportunityMapper implements RowMapper {
    @Override
    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        TestOpportunity testOpportunity = new TestOpportunity();
        testOpportunity.setKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("key")));
        testOpportunity.setSessionKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("sessionKey")));
        testOpportunity.setBrowserKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("browserKey")));
        testOpportunity.setTestKey(resultSet.getString("testKey"));
        testOpportunity.setTestee(resultSet.getDouble("testee"));
        testOpportunity.setTestId(resultSet.getString("testId"));
        testOpportunity.setAdminSubject(resultSet.getString("test"));
        testOpportunity.setStatus(resultSet.getString("status"));
        testOpportunity.setDateStarted(resultSet.getDate("dateStarted"));
        testOpportunity.setDateChanged(resultSet.getDate("dateChanged"));
        testOpportunity.setRestartCount(resultSet.getInt("rcnt"));
        testOpportunity.setGracePeriodRestarts(resultSet.getInt("gpRestarts"));
        testOpportunity.setMaxItems(resultSet.getInt("testLength"));
        testOpportunity.setSubject(resultSet.getString("subject"));
        testOpportunity.setClientName(resultSet.getString("clientName"));

        return testOpportunity;
    }
}
