package tds.student.performance.dao.mappers;

import org.apache.xpath.operations.Bool;
import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.domain.ClientTestMode;
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
public class ClientTestModeMapper implements RowMapper<ClientTestMode> {
    @Override
    public ClientTestMode mapRow(ResultSet resultSet, int i) throws SQLException {
        ClientTestMode clientTestMode = new ClientTestMode();
        clientTestMode.setKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("key")));
        clientTestMode.setClientName(resultSet.getString("clientName"));
        clientTestMode.setTestId(resultSet.getString("testId"));
        clientTestMode.setMode(resultSet.getString("mode"));
        clientTestMode.setAlgorithm(resultSet.getString("algorithm"));
        clientTestMode.setFormTideSelectable((Boolean)resultSet.getObject("formTideSelectable"));
        clientTestMode.setIsSegmented((Boolean)resultSet.getObject("isSegmented"));
        clientTestMode.setMaxOpps((Integer)resultSet.getObject("maxOpps"));
        clientTestMode.setRequireRtsForm((Boolean)resultSet.getObject("requireRtsForm"));
        clientTestMode.setRequireRtsFormWindow((Boolean)resultSet.getObject("requireRtsFormWindow"));
        clientTestMode.setRequireRtsFormIfExists((Boolean)resultSet.getObject("requireRtsFormIfExists"));
        clientTestMode.setSessionType((Integer)resultSet.getObject("sessionType"));
        clientTestMode.setTestKey(resultSet.getString("testKey"));

        return clientTestMode;
    }
}
