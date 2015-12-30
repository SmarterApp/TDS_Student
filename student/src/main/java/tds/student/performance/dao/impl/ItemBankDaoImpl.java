package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.student.performance.dao.ItemBankDao;
import tds.student.performance.domain.SetOfAdminSubject;

import javax.sql.DataSource;
import java.util.HashMap;
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
}
