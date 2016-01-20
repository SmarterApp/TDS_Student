package tds.student.performance.dao;

import org.springframework.stereotype.Repository;
import tds.student.performance.domain.TestAbility;

import java.util.List;
import java.util.UUID;

/**
 * Created by emunoz on 12/28/15.
 */
public interface TestAbilityDao {
    /**
     * Gets the list of {@link TestAbility} objects from the testopportunity and testopportunityscores tables
     *
     * @param oppKey
     * @param clientname
     * @param subject
     * @param testee
     * @return
     */
    List<TestAbility> getTestAbilities(UUID oppKey, String clientname, String subject, Long testee);

    /**
     * Gets the most recent initialAbility from the testeehistory table
     * @param clientname
     * @param subject
     * @param testee
     * @return
     */
    Float getMostRecentTestAbilityFromHistory(String clientname, String subject, Long testee);
}
