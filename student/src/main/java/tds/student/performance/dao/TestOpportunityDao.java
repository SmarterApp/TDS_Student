package tds.student.performance.dao;

import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestOpportunityAudit;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Created by jjohnson on 12/24/15.
 */
public interface TestOpportunityDao {
    TestOpportunity get(UUID key);
    List<TestOpportunity> getBySessionAndStatus(UUID sessionKey, String statusUsage, String statusStage);
    void update(TestOpportunity opportunity);

    Timestamp getLastActivity(UUID key);
    void createAudit(TestOpportunityAudit testOpportunityAudit);
}