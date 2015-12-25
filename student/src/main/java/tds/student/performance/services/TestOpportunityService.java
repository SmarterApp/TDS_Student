package tds.student.performance.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.dao.TestOpportunityDao;
import tds.student.performance.dao.SessionAuditDao;
import tds.student.performance.dao.TestSessionDao;
import tds.student.performance.dao.utils.HostNameHelper;
import tds.student.performance.domain.SessionAudit;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSession;
import tds.student.sql.data.OpportunityInstance;

import java.util.Date;
import java.util.List;

/**
 * A service for interacting with a {@code TestOpportunity}.
 */
public class TestOpportunityService {
    private static final Logger logger = LoggerFactory.getLogger(TestOpportunityService.class);

    @Autowired
    TestOpportunityDao testOpportunityDao;

    @Autowired
    TestSessionDao testSessionDao;

    @Autowired
    SessionAuditDao sessionAuditDao;

    public void startTestOpportunity(OpportunityInstance opportunityInstance, String testKey, List<String> formKeys) {
        try {
            TestOpportunity testOpportunity = testOpportunityDao.get(opportunityInstance.getKey());
            if (testOpportunity == null) {
                // TODO: Handle when testOpportunity is null
            }

            verifyTesteeAccess(testOpportunity, opportunityInstance);
        } catch(IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * This method emulates the functionality and logic contained in {@code StudentDLL._ValidateTesteeAccessProc_SP}.
     * @param testOpportunity the {@code TestOpportunity} attempting to start.
     * @param opportunityInstance the {@code OpportunityInstance} attempting to start the {@code TestOpportunity}.
     */
    private void verifyTesteeAccess(TestOpportunity testOpportunity, OpportunityInstance opportunityInstance) throws IllegalArgumentException {
        Date now = new Date();

        // Emulate logic on line 492 of _ValidateTesteeAccessProc_SP in StudentDLL.class
        // RULE:  The test opportunity's browser key must match the opportunity instance's browser key.
        if (!testOpportunity.getBrowserKey().equals(opportunityInstance.getBrowserKey())) {
            throw new IllegalArgumentException(String.format("testOpportunity.getBrowserKey() %s does not match opportunityInstance.getBrowserKey() %s", testOpportunity.getBrowserKey(), opportunityInstance.getBrowserKey()));
        }

        // Emulate logic on line 555 of _ValidateTesteeAccessProc_SP in StudentDLL.class
        // RULE:  The test opportunity's session key must match the opportunity instance's session key.
        if (!testOpportunity.getSessionKey().equals(opportunityInstance.getSessionKey())) {
            throw new IllegalArgumentException(String.format("testOpportunity.getSessionKey() %s does not match opportunityInstance.getSessionKey() %s", testOpportunity.getSessionKey(), opportunityInstance.getSessionKey()));
        }

        TestSession testSession = testSessionDao.get(opportunityInstance.getSessionKey());
        if (testSession == null) {
            logger.error(String.format("Could not find TestSession record in session.session for key %s", opportunityInstance.getSessionKey()));
            // TODO: Handle when testSession is null
        }

        // Emulate logic on line 523 of _ValidateTesteeAccessProc_SP in StudentDLL.class
        // RULE:  The test session must be open in order for the student to start the test.
        if (!testSession.isOpen(now)) {
            throw new IllegalArgumentException(String.format("TestSession.isOpen for session key %s and date %s is false.", testSession.getKey(), now));
        }

        // Emulate logic on line 529 of _ValidateTesteeAccessProc_SP in StudentDLL.class.  Apparently, having a NULL
        // proctor value for the test session is okay...?  In the legacy method,  _ValidateTesteeAccessProc_SP in StudentDLL.class
        // only fails if the error has some message in it.  This call in the  _ValidateTesteeAccessProc_SP in StudentDLL.class
        // just returns without assigning a message to the error argument.
        if (testSession.getProctorId() == null) {
            logger.warn(String.format("TestSession for session key %s has a NULL proctor value.", testSession.getKey()));
            return;
        }

        // Emulate logic on line 533 of _ValidateTesteeAccessProc_SP in StudentDLL.class.
        Integer checkIn = testSessionDao.getCheckIn(testSession.getClientName());
        if (checkIn == null || checkIn == 0) {
            throw new IllegalArgumentException(String.format("Check in value for TestSession with client name '%s' %s", testSession.getClientName(), checkIn == null ? "is null" : "is 0"));
        }

        // Emulate logic on line 533 of _ValidateTesteeAccessProc_SP in StudentDLL.class.
        // RULE:  Student should not be able to start a test if the timeout window has expired.
        Date dateVisitedPlusCheckIn = new Date(testSession.getDateVisited().getTime() + checkIn);
        if (now.after(dateVisitedPlusCheckIn)) {
            sessionAuditDao.create(new SessionAudit(
                    testSession.getKey(),
                    now,
                    "TACheckin TIMEOUT",
                    HostNameHelper.getHostName(),
                    testSession.getSessionBrowser(),
                    "session"
            ));

            new TestSessionService().pause(testOpportunity, testSession, "closed");

            throw new IllegalArgumentException(String.format("TestSession for session key %s is not available for testing.", testSession.getKey()));
        }
    }
}
