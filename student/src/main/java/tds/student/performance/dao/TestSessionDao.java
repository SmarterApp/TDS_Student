package tds.student.performance.dao;

import tds.student.performance.domain.TestSession;
import tds.student.performance.domain.TestSessionTimeLimitConfiguration;

import java.util.List;
import java.util.UUID;

/**
 * Created by jjohnson on 12/24/15.
 */
public interface TestSessionDao {
    TestSession get(UUID key);
    void pause(TestSession session, String reason);
    List<TestSessionTimeLimitConfiguration> getTimeLimitConfigurations(String clientName, String testId);
}
