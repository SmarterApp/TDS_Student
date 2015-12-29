package tds.student.performance.services;

import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSession;
import tds.student.performance.domain.TestSessionTimeLimitConfiguration;

import java.util.UUID;

/**
 * Created by jjohnson on 12/26/15.
 */
public interface TestSessionService {
    TestSession get(UUID key);
    void pause(TestOpportunity testOpportunity, TestSession testSession, String reason);
    Integer getCheckInTimeLimit(String clientName);
    TestSessionTimeLimitConfiguration getTimelimitConfiguration(String clientName, String testId);
}
