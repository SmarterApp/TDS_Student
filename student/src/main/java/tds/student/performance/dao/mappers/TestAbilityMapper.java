package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.domain.TestAbility;
import tds.student.performance.utils.UuidAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by emunoz on 12/28/15.
 */
public class TestAbilityMapper implements RowMapper<TestAbility> {
    @Override
    public TestAbility mapRow(ResultSet resultSet, int i) throws SQLException {
        TestAbility ability = new TestAbility();
        ability.setOppkey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("oppkey")));
        ability.setDateScored(resultSet.getTimestamp("dateScored"));
        ability.setOpportunity(resultSet.getInt("opportunity"));
        ability.setTest(resultSet.getString("test"));
        ability.setScore(resultSet.getFloat("score"));
        return ability;
    }
}
