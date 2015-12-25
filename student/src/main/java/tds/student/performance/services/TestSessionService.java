package tds.student.performance.services;

import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.TestOpportunityAuditDao;
import tds.student.performance.dao.SessionAuditDao;
import tds.student.performance.dao.TestSessionDao;
import tds.student.performance.dao.utils.HostNameHelper;
import tds.student.performance.domain.SessionAudit;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestOpportunityAudit;
import tds.student.performance.domain.TestSession;

import java.util.Date;

/**
 * A service for interacting with a {@code TestSession}.
 */
public class TestSessionService {
    @Autowired
    TestSessionDao testSessionDao;

    @Autowired
    SessionAuditDao sessionAuditDao;

    @Autowired
    TestOpportunityAuditDao testOpportunityAuditDao;

    /**
     * Pause a {@code TestSession} for a {@code TestOpportunity}.
     * <p>
     *     This method will also update the appropriate audit tables with records to indicate the {@code TestSession}
     *     was paused.
     * </p>
     * @param testOpportunity The {@code TestOpportunity} for which the {@code TestSession} should be paused.
     * @param testSession The {@code TestSession} to pause.
     * @param reason A description of why the {@code TestSession} was put in a paused state.
     */
    public void pause(TestOpportunity testOpportunity, TestSession testSession, String reason) {
        final Date now = new Date();
        testSessionDao.pause(testSession, reason); // TODO: enumerate reasons?  Add reason getter/setter to TestSession?

        sessionAuditDao.create(new SessionAudit(
                testSession.getKey(),
                now,
                reason,
                HostNameHelper.getHostName(),
                testSession.getSessionBrowser(),
                "session"
        ));

        testOpportunityAuditDao.create(new TestOpportunityAudit(
                testOpportunity.getKey(),
                now,
                "paused by session",
                testSession.getKey(),
                HostNameHelper.getHostName(),
                "session"
        ));

        // TODO:  Update any other opportunities (Starting @ line 1750 in CommonDLL.P_PauseSession_SP
    }
}
