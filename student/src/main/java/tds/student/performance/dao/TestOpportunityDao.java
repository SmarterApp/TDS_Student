package tds.student.performance.dao;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.performance.domain.TestOpportunity;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by jjohnson on 12/24/15.
 */
public interface TestOpportunityDao {
    TestOpportunity get(UUID key);
    List<TestOpportunity> getTestOpportunitiesBySessionAndStatus(UUID sessionKey, String statusUsage, String statusStage);
    void legacySetOpportunityStatus(TestOpportunity opportunity, String status) throws SQLException, ReturnStatusException;
}
