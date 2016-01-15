package tds.student.performance.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.StudentDao;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class StudentDaoImpl implements StudentDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<String> findStudentId(int key) {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("key", key);

        final String query =
                "select sk.studentid \n" +
                        "from session.r_studentkeyid sk \n " +
                        "where sk.studentkey = :key ";


        return namedParameterJdbcTemplate.queryForList(query, parameters, String.class);
    }


}
