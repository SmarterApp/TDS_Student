package tds.student.performance.services;

import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSession;

/**
 * Created by jjohnson on 12/26/15.
 */
public interface TestSessionService {
    void pause(TestOpportunity testOpportunity, TestSession testSession, String reason);

}
