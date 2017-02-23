package tds.student.performance.dao;

import java.util.UUID;

/**
 * Used to store mapping from legacy test opportunity IDs to the new exam IDs.  This is used when in the mode of calling
 * both the legacy services as well as the new services.  Eventually this will be removed.
 */
public interface TestOpportunityExamMapDao {

    /**
     * Insert a record mapping the test opportunity ID and exam ID
     * @param testOpportunityId test opportunity ID from session.testopportunity table
     * @param examId exam ID used in the new exam.exam table
     */
    void insert(final UUID testOpportunityId, final UUID examId);
}
