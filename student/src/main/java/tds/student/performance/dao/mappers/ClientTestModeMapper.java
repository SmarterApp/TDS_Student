package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.domain.ClientTestMode;
import tds.student.performance.utils.UuidAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jjohnson on 1/3/16.
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
        clientTestMode.setFormTideSelectable(resultSet.getBoolean("formTideSelectable"));
        clientTestMode.setIsSegmented(resultSet.getBoolean("isSegmented"));
        clientTestMode.setMaxOpps(resultSet.getInt("maxOpps"));
        clientTestMode.setRequireRtsForm(resultSet.getBoolean("requireRtsForm"));
        clientTestMode.setRequireRtsFormWindow(resultSet.getBoolean("requireRtsFormWindow"));
        clientTestMode.setRequireRtsFormIfExists(resultSet.getBoolean("requireRtsFormIfExists"));
        clientTestMode.setSessionType(resultSet.getInt("sessionType"));
        clientTestMode.setTestKey(resultSet.getString("testKey"));

        return clientTestMode;
    }
}
