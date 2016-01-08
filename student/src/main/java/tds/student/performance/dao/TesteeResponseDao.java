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

    /**
     * Gets the total count of testee response items for the specified opportunity key.
     *
     * @param oppKey
     * @return
     */
    Long getTesteeResponseItemCount(UUID oppKey);

    /**
     * Deletes a record from the session.testeeresponse table
     *
     * @param oppKey
     */
    void delete(UUID oppKey);

    /**
     * Batch inserts multiple records from 1 to maxPosition into the testeeresponse table.
     * @param oppKey
     * @param maxPosition
     */
    void insertBatch(UUID oppKey, final Integer maxPosition);
}
