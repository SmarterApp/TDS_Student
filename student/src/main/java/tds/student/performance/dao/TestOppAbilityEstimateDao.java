package tds.student.performance.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.student.performance.domain.TestOppAbilityEstimate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by emunoz on 1/4/16.
 */
public interface TestOppAbilityEstimateDao {
    /**
     * Creates and inserts a {@link TestOppAbilityEstimate} record into the session.testoppabilityestimate
     * table.
     *
     * @param estimate the object representing a row in the testoppabilityestimate table
     */
    void create(TestOppAbilityEstimate estimate);


    /**
     * Creates and inserts a record into the session.testoppabilityestimate by obtaining the
     * strand value from the itembankdb and testopportunity tables.
     *
     * @param oppKey
     * @param ability
     * @param date
     */
    void createFromItemBankAndTestOpp(UUID oppKey, Float ability, Timestamp date);
}
