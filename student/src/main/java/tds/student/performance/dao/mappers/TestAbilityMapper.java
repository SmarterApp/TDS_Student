package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.domain.TestAbility;
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
public class TestAbilityMapper implements RowMapper<TestAbility> {
    @Override
    public TestAbility mapRow(ResultSet resultSet, int i) throws SQLException {
        TestAbility ability = new TestAbility();
        ability.setOppkey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("oppkey")));
        ability.setDateScored(resultSet.getTimestamp("dateScored"));
        ability.setOpportunity((Integer)resultSet.getObject("opportunity"));
        ability.setTest(resultSet.getString("test"));
        ability.setScore((Float)resultSet.getObject("score", Float.class));
        return ability;
    }
}
