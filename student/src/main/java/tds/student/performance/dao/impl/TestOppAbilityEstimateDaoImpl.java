package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.TestOppAbilityEstimateDao;
import tds.student.performance.domain.TestOppAbilityEstimate;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by emunoz on 1/4/16.
 */
@Repository
public class TestOppAbilityEstimateDaoImpl implements TestOppAbilityEstimateDao {
    private static final Logger logger = LoggerFactory.getLogger(TestOppAbilityEstimateDaoImpl.class);
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional
    public void create(TestOppAbilityEstimate estimate) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(estimate.getOppKey()));
        parameters.put("strand", estimate.getStrand());
        parameters.put("ability", estimate.getAbility());
        parameters.put("itemPosition", estimate.getItemPosition());
        parameters.put("date", estimate.getDate());

        final String SQL =
                "INSERT INTO\n" +
                    "session.testoppabilityestimate (\n" +
                        "_fk_TestOpportunity,\n" +
                        "strand,\n" +
                        "estimate,\n" +
                        "itemPos,\n" +
                        "_date)\n" +
                    "VALUES (\n" +
                        ":oppKey,\n" +
                        ":strand,\n" +
                        ":ability,\n" +
                        ":itemPosition,\n" +
                        ":date)";

            try {
                namedParameterJdbcTemplate.update(SQL, parameters);
            } catch (DataAccessException e) {
                logger.error(String.format("%s INSERT threw exception", SQL), e);
                throw e;
            }
    }

    @Override
    @Transactional
    public void createFromItemBankAndTestOpp(UUID oppKey, Float ability, Timestamp date) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));
        parameters.put("ability", ability);
        parameters.put("date", date);

        final String SQL =
            "INSERT INTO\n" +
                "session.testoppabilityestimate (\n" +
                    "_fk_TestOpportunity,\n" +
                    "strand,\n" +
                    "estimate,\n" +
                    "itemPos,\n" +
                    "_date)\n" +
                "SELECT\n" +
                    ":oppKey,\n" +
                    "_fk_Strand,\n" +
                    ":ability,\n" +
                    "0,\n" +
                    ":date\n" +
                "FROM\n" +
                    "itembank.tbladminstrand S, session.testopportunity O\n" +
                "WHERE\n" +
                    "O._key = :oppKey AND\n" +
                    "O._efk_AdminSubject = S._fk_AdminSubject AND\n" +
                    "S.startAbility is not null";

        try {
            namedParameterJdbcTemplate.update(SQL, parameters);
        } catch (DataAccessException e) {
            logger.error(String.format("%s INSERT threw exception", SQL), e);
            throw e;
        }
    }

}
