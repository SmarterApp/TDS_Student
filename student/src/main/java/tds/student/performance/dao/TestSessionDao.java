package tds.student.performance.dao;

import tds.student.performance.domain.TestSession;

import java.util.UUID;

/**
 * Created by jjohnson on 12/24/15.
 */
public interface TestSessionDao {
    TestSession get(UUID key);
    Integer getCheckIn(String clientName);
    void pause(TestSession session, String reason);
}
