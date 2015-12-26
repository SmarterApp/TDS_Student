package tds.student.performance.services;

import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.dao.TestOpportunityAuditDao;
import tds.student.performance.dao.SessionAuditDao;
import tds.student.performance.dao.TestSessionDao;
import tds.student.performance.utils.HostNameHelper;
import tds.student.performance.domain.*;

import java.util.Date;
import java.util.List;

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

    @Autowired
    ConfigurationDao configurationDao;

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

        // TODO:  Get these values from cache.
        List<ClientSystemFlag> systemFlags = configurationDao.getSystemFlags(testOpportunity.getClientName());

        // TODO:  Implement this to make sure the proctor session is valid: CommonDLL.ValidateProctorSession_FN
        testSessionDao.pause(testSession, reason); // TODO: enumerate reasons?  Add reason getter/setter to TestSession?

        if (clientSystemFlagIsOn(systemFlags, "sessions", testOpportunity.getClientName())) {
            sessionAuditDao.create(new SessionAudit(
                    testSession.getKey(),
                    now,
                    reason,
                    HostNameHelper.getHostName(),
                    testSession.getSessionBrowser(),
                    "session"
            ));
        }

        if (clientSystemFlagIsOn(systemFlags, "opportunities", testOpportunity.getClientName())) {
            testOpportunityAuditDao.create(new TestOpportunityAudit(
                    testOpportunity.getKey(),
                    now,
                    "paused by session",
                    testSession.getKey(),
                    HostNameHelper.getHostName(),
                    "session"
            ));
        }

        // TODO:  Update any other opportunities (Starting @ line 1750 in CommonDLL.P_PauseSession_SP.  The code starting @ line 1750 has a number of complexities for setting the correct status.
    }

    /**
     * Determine if a {@code ClientSystemFlag} is enabled for the specified audit object (i.e. the name of the flag in
     * question) and client name combination.
     * <p>
     *     !!!! CODE SMELL !!!!
     *     This should be moved into some sort of helper and/or service for dealing with configuration settings.  It can
     *     live here for now, but this should definitely be refactored.
     * </p>
     * @param systemFlags The collection of {@code ClientSystemFlag} records to inspect.
     * @param auditObject The name of the audit object to look for.
     * @param clientName The name of the client.
     * @return {@code True} if the specified audit object is set to "On" for the client; otherwise {@code False}.
     */
    private Boolean clientSystemFlagIsOn(List<ClientSystemFlag> systemFlags, String auditObject, String clientName) {
        if (systemFlags == null || systemFlags.size() == 0) {
            return false;
        }

        ClientSystemFlag flagToFind = new ClientSystemFlag(auditObject, clientName);

        return systemFlags.contains(flagToFind)
                ? systemFlags.get(systemFlags.indexOf(flagToFind)).getIsOn()
                : false;
    }
}
