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
    TestSessionTimeLimitConfiguration getTimeLimitConfiguration(String clientName);
    TestSessionTimeLimitConfiguration getTimeLimitConfiguration(String clientName, String testId);
    void pause(TestSession session, String reason);
    String validateProctorSession(TestSession testSession);
    String validateProctorSession(TestSession testSession, Long proctorKey, UUID browserKey);

}
