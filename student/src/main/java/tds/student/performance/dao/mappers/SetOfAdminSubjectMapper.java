package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.domain.SetOfAdminSubject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SetOfAdminSubjectMapper implements RowMapper<SetOfAdminSubject> {
    @Override
    public SetOfAdminSubject mapRow(ResultSet resultSet, int i) throws SQLException {
        SetOfAdminSubject setOfAdminSubject = new SetOfAdminSubject();
        setOfAdminSubject.setKey(resultSet.getString("key"));
        setOfAdminSubject.setMaxItems(resultSet.getInt("maxitems"));
        setOfAdminSubject.setSegmented(resultSet.getInt("isSegmented") == 1);
        setOfAdminSubject.setSelectionAlgorithm(resultSet.getString("selectionAlgorithm"));
        setOfAdminSubject.setTestId(resultSet.getString("testId"));
        setOfAdminSubject.setStartAbility(resultSet.getDouble("startAbility"));

        return setOfAdminSubject;
    }
}