package tds.student.performance.dao;

import java.util.UUID;

public interface TestOpportunityExamMapDao {
    void insert(UUID testOpportunityId, UUID examId);
}
