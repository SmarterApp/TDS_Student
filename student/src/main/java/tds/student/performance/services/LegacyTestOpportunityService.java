package tds.student.performance.services;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.performance.domain.TestOpportunity;

import java.sql.SQLException;

public interface LegacyTestOpportunityService {
    void setOpportunityStatus(TestOpportunity opportunity, String status) throws SQLException, ReturnStatusException;
}
