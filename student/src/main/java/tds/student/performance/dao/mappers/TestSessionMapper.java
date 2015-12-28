package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.TestSession;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Map a record from the {@code session.session} table to a {@code TestSession} object.
 */
public class TestSessionMapper implements RowMapper<TestSession> {

    @Override
    public TestSession mapRow(ResultSet resultSet, int i) throws SQLException {
        TestSession testSession = new TestSession();
        testSession.setKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("key")));
        testSession.setSessionType(resultSet.getInt("sessionType"));
        testSession.setStatus(resultSet.getString("status"));
        testSession.setDateBegin(resultSet.getDate("dateBegin"));
        testSession.setDateEnd(resultSet.getDate("dateEnd"));
        testSession.setDateVisited(resultSet.getDate("dateVisited"));
        testSession.setClientName(resultSet.getString("clientName"));
        testSession.setProctorId(resultSet.getDouble("proctor"));
        testSession.setSessionBrowser(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("sessionBrowser")));

        return testSession;
    }
}
