package tds.student.performance.dao;

import tds.student.performance.domain.UnfinishedResponsePage;

import java.util.List;
import java.util.UUID;

/**
 * Created by emunoz on 12/30/15.
 */
public interface TesteeResponseDao {

    /**
     * Updates the opportunityRestart value for a testeeresponse row
     *
     * @param opportunityKey the opportunity key for which to update
     * @param restartCount the restart count to increment
     * @param isRcntSpecific if true, adds optional where clause
     */
    void updateRestartCount(UUID opportunityKey, Integer restartCount, boolean isRcntSpecific);

    /**
     * Retrieves a list of unfinished response pages for a specific opportunity
     * @param opportunityKey
     * @return
     */
    List<UnfinishedResponsePage> getUnfinishedPages(UUID opportunityKey);
}
