package tds.student.performance.dao;

import java.util.UUID;

public interface TestOpportunityExamMapDao {
    void insert(final UUID testOpportunityId, final UUID examId);
}
