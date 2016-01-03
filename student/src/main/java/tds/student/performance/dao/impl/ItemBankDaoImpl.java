package tds.student.performance.dao.impl;

import AIR.Common.DB.SqlParametersMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.student.performance.dao.ItemBankDao;
import tds.student.performance.domain.SetOfAdminSubject;
import tds.student.sql.data.TestGrade;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for interacting with the {@code itembank} database.
 * <p>
 *     This class could potentially be used for all interaction with the {@code itembank} database.  Normally the DAO
 *     pattern tends to define one DAO object per table.  In this case, it might make more sense to just have one
 *     general DAO that allows us to talk to the {@code itembank} database and get whatever we need.
 * </p>
 */
@Repository
public class ItemBankDaoImpl implements ItemBankDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationDaoImpl.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public SetOfAdminSubject get(String adminSubject) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("adminSubject", adminSubject);

        final String SQL =
                "SELECT\n" +
                    "_key AS `key`,\n" +
                    "maxitems AS maxItems,\n" +
                    "startability AS startAbility\n" +
                "FROM\n" +
                    "itembank.tblsetofadminsubjects\n" +
                "WHERE\n" +
                    "_key = :adminSubject";

        try{
            return namedParameterJdbcTemplate.queryForObject(
                    SQL,
                    parameters,
                    new BeanPropertyRowMapper<>(SetOfAdminSubject.class));
        } catch(EmptyResultDataAccessException e) {
            logger.warn(String.format("%s did not return results for adminSubject = %s", SQL, adminSubject));
            return null;
        }
    }

    @Override
    public List<TestGrade> getTestGrades(String clientName, String testKey, Integer sessionType) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put ("testKey", testKey);
        parameters.put ("clientName", clientName);
        parameters.put ("sessionType", sessionType);

        final String SQL = "select distinct grade "
                + " from itembank.tblsetofadminsubjects S, configs.client_testmode M, configs.client_testgrades G, configs.client_testwindow W, configs.client_testproperties P "
                + " where M.clientname = :clientName and (:testKey is null or M.testkey = :testKey) and M.testkey = S._Key and (M.sessionType = -1 or M.sessionType = :sessionType) "
                + "    and M.clientname = G.clientname and M.TestID = G.TestID  and W.clientname = :clientName and W.TestID = M.testID and P.clientname = :clientName and P.TestID = M.testID "
                + "    and P.IsSelectable = 1 and now() between W.startDate and W.endDate order by grade";

        List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(
                SQL,
                parameters);

        List<TestGrade> results = new ArrayList<>();

        for (Map<String, Object> row : list) {
            results.add(new TestGrade((String)row.get("grade")));
        }

        return results;
    }
}
