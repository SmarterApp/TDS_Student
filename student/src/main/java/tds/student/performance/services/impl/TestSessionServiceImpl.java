package tds.student.performance.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.dao.TestOpportunityAuditDao;
import tds.student.performance.dao.SessionAuditDao;
import tds.student.performance.dao.TestSessionDao;
import tds.student.performance.services.TestSessionService;
import tds.student.performance.utils.HostNameHelper;
import tds.student.performance.domain.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A service for interacting with a {@code TestSession}.
 */
@Service
public class TestSessionServiceImpl implements TestSessionService {
    @Autowired
    TestSessionDao testSessionDao;

    @Autowired
    SessionAuditDao sessionAuditDao;

    @Autowired
    TestOpportunityAuditDao testOpportunityAuditDao;

    @Autowired
    ConfigurationDao configurationDao;

    /**
     * Get a {@code TestSession} for the specified key.
     * @param key The session key for which a {@code TestSession} should be fetched.
     * @return A {@code TestSession} for the specified key.
     */
    public TestSession get(UUID key) {
        return testSessionDao.get(key);
    }

    /**
     * Get the TA Check-In time limit for the specified client.
     * <p>
     *     This is a special method to fetch the {@code tacheckintime} from the {@code session.timelimits} view.  Unlike
     *     the other timeout values, the code in {@code StudentDLL._ValidateTesteeAccessProc_SP} on line 533 specifically
     *     queries the {@code tacheckintime} view where the test id is null.  The assumption here is that the
     *     TA Check-In time is always configured at the client level.  Furthermore, the TA Check-In time setting at the
     *     client level supercedes any setting at the test level.  This is the only value in {@code session.timelimits}
     *     that exhibits this behavior.
     * </p>
     * <p>
     *     Needs manual caching if this method is called internally (that is somewhere else within this class).
     * </p>
     * @param clientName The name of the client for which the TA Check-In Time should be fetched.
     * @return An {@code Integer} representing the length of the TA Check-In timeout window.
     */
    @Override
    public Integer getCheckInTimeLimit(String clientName) {
        return this.getTimelimitConfiguration(clientName, null).getTaCheckinTime();
    }

    /**
     * Get the {@code TestSessionTimelimitConfiguration} configuration values from the {@code session.timelimits} view.
     * <p>
     *     The logic in {@code StudentDLL.T_StartTestOpportunity_SP} starting at line 3668 is as follows:
     *     -- Attempt to find a record in {@code session.timelimits} for the specified test id and client name.
     *     -- If there is no such record, find a record in {@code session.timelimits} for the specified client name
     *        where the test id is null.
     *
     *     The assumption for this logic is that the program attempts to find time limit values that are specific to the
     *     test being taken, otherwise it falls back the client-wide time limit settings.
     * </p>
     * <p>
     *     When the {@code session} database is loaded with seed data, all records returned by the {@code timelimits}
     *     view have a null test id.
     * </p>
     * <p>
     *     <strong>NOTE:</strong> Strong candidate for caching.
     * </p>
     * @param clientName the name of the client for which the {@code TestSessionTimelimitConfiguration} should be fetched.
     * @param testId The Id of the test for which the {@code TestSessionTimelimitConfiguration} should be fetched.
     * @return A {@code TestSessionTimelimitConfiguration} for the test and client name.
     */
    @Override
    public TestSessionTimeLimitConfiguration getTimelimitConfiguration(String clientName, String testId) {
        List<TestSessionTimeLimitConfiguration> timelimits = testSessionDao.getTimeLimitConfigurations(clientName, testId);

        TestSessionTimeLimitConfiguration timelimitToFind = new TestSessionTimeLimitConfiguration(clientName, testId);

        return timelimits.contains(timelimitToFind)
                ? timelimits.get(timelimits.indexOf(timelimitToFind))
                : timelimits.get(0);
    }

    /**
     * Pause a {@code TestSession} for a {@code TestOpportunity}.
     * <p>
     *     This method will also update the appropriate audit tables with records to indicate the {@code TestSession}
     *     was paused.
     * </p>
     * <p>
     *     <strong>NOTE:</strong> Strong candidate for caching.
     * </p>
     * @param testOpportunity The {@code TestOpportunity} for which the {@code TestSession} should be paused.
     * @param testSession The {@code TestSession} to pause.
     * @param reason A description of why the {@code TestSession} was put in a paused state.
     */
    @Override
    public void pause(TestOpportunity testOpportunity, TestSession testSession, String reason) {
        final Timestamp now = new Timestamp(new Date().getTime());

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

        // TODO:  Update other appropriate opportunities (Starting @ line 1750 in CommonDLL.P_PauseSession_SP.  The code starting @ line 1750 has a number of complexities for setting the correct status.
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
            // TODO:  throw exception instead.
        }

        ClientSystemFlag flagToFind = new ClientSystemFlag(auditObject, clientName);

        return systemFlags.contains(flagToFind)
                ? systemFlags.get(systemFlags.indexOf(flagToFind)).getIsOn()
                : false;
    }
}
