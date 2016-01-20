package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.domain.SetOfAdminSubject;

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
public class SetOfAdminSubjectMapper implements RowMapper<SetOfAdminSubject> {
    @Override
    public SetOfAdminSubject mapRow(ResultSet resultSet, int i) throws SQLException {
        SetOfAdminSubject setOfAdminSubject = new SetOfAdminSubject();
        setOfAdminSubject.setKey(resultSet.getString("key"));
        setOfAdminSubject.setMaxItems((Integer)resultSet.getObject("maxitems"));
        setOfAdminSubject.setSegmented((Boolean)resultSet.getObject("isSegmented"));
        setOfAdminSubject.setSelectionAlgorithm(resultSet.getString("selectionAlgorithm"));
        setOfAdminSubject.setTestId(resultSet.getString("testId"));
        setOfAdminSubject.setStartAbility((Float)resultSet.getObject("startAbility"));

        return setOfAdminSubject;
    }
}