package tds.student.performance.services;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSession;
import tds.student.performance.domain.TestSessionTimeLimitConfiguration;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by jjohnson on 12/26/15.
 */
public interface TestSessionService {
    TestSession get(UUID key);
    void pause(TestOpportunity testOpportunity, TestSession testSession) throws SQLException, ReturnStatusException;
    void pause(TestOpportunity testOpportunity, TestSession testSession, String reason) throws SQLException, ReturnStatusException;
    Integer getCheckInTimeLimit(String clientName);
    TestSessionTimeLimitConfiguration getTimelimitConfiguration(String clientName, String testId);
}
