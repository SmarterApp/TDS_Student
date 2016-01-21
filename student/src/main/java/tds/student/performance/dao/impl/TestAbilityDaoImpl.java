package tds.student.performance.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.caching.CacheType;
import tds.student.performance.dao.TestAbilityDao;
import tds.student.performance.dao.mappers.TestAbilityMapper;
import tds.student.performance.domain.TestAbility;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.*;

/**
 * Created by emunoz on 12/28/15.
 */
@Repository
public class TestAbilityDaoImpl implements TestAbilityDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<TestAbility> getTestAbilities(UUID oppKey, String clientname, String subject, Long testee) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppkey", UuidAdapter.getBytesFromUUID(oppKey));
        parameters.put("clientname", clientname);
        parameters.put("subject", subject);
        parameters.put("testee", testee);

        final String SQL =
                "SELECT\n" +
                    "OTHEROPP._Key as oppkey,\n" +
                    "OTHEROPP._efk_TestID as test,\n" +
                    "OTHEROPP.Opportunity as opportunity,\n" +
                    "OTHEROPP.dateScored as dateScored,\n" +
                    "SCORE.value as score\n" +
                "FROM\n" +
                    "session.testopportunity OTHEROPP, session.testopportunityscores SCORE\n" +
                "WHERE\n" +
                    "clientname = :clientname AND\n" +
                    "OTHEROPP._efk_Testee = :testee AND\n" +
                    "OTHEROPP.subject = :subject AND\n" +
                    "OTHEROPP.dateDeleted is null AND\n" +
                    "OTHEROPP.dateScored is not null AND\n" +
                    "OTHEROPP._Key <> :oppkey AND\n" +
                    "OTHEROPP._Key = SCORE._fk_TestOpportunity AND\n" +
                    "SCORE.UseForAbility = 1 AND\n" +
                    "SCORE.value is not null";

        return namedParameterJdbcTemplate.query(SQL, parameters, new TestAbilityMapper());
    }

    @Override
    public Float getMostRecentTestAbilityFromHistory(String clientname, String subject, Long testee) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientname", clientname);
        parameters.put("subject", subject);
        parameters.put("testee", testee);

        final String SQL =
                "SELECT\n" +
                    "MAX(initialAbility) as ability\n" +
                "FROM\n" +
                    "session.testeehistory\n" +
                "WHERE\n" +
                    "clientname = :clientname AND\n" +
                    "_efk_Testee = :testee AND\n" +
                    "Subject = :subject AND\n" +
                    "initialAbility is not null;";

        return namedParameterJdbcTemplate.queryForObject(SQL, parameters, Float.class);
    }
}
