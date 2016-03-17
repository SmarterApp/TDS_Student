/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
package tds.student.performance.services.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.student.performance.dao.*;
import tds.student.performance.exceptions.ReturnErrorException;
import tds.student.performance.services.ConfigurationService;
import tds.student.performance.services.DbLatencyService;
import tds.student.performance.services.LegacyTestOpportunityService;
import tds.student.performance.services.TestSessionService;
import tds.student.performance.utils.DateUtility;
import tds.student.performance.utils.HostNameHelper;
import tds.student.performance.domain.*;

import java.sql.SQLException;
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
    TestOpportunityDao testOpportunityDao;

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    LegacyTestOpportunityService legacyTestOpportunityService;

    @Autowired
    LegacyErrorHandlerServiceImpl legacyErrorHandlerService;

    @Autowired
    DbLatencyService dbLatencyService;

    @Autowired
    DateUtility dateUtility;

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
        TestSessionTimeLimitConfiguration timeLimitConfiguration = testSessionDao.getTimeLimitConfiguration(clientName);

        return timeLimitConfiguration == null
                ? 0
                : timeLimitConfiguration.getTaCheckinTimeMinutes();
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
        TestSessionTimeLimitConfiguration timeLimitConfiguration = testSessionDao.getTimeLimitConfiguration(clientName, testId);

        if (timeLimitConfiguration == null) {
            return testSessionDao.getTimeLimitConfiguration(clientName);
        }

        return timeLimitConfiguration;
    }

    @Override
    public void createAudit(SessionAudit sessionAudit) {
        testSessionDao.createAudit(sessionAudit);
    }

    @Override
    public void pause(TestOpportunity testOpportunity, TestSession testSession) throws SQLException, ReturnStatusException, ReturnErrorException {
        pause(testOpportunity, testSession, "closed");
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
    public void pause(TestOpportunity testOpportunity, TestSession testSession, String reason) throws SQLException, ReturnStatusException, ReturnErrorException {
        final Timestamp now = new Timestamp(dateUtility.getDbDate().getTime());

        // TODO:  Implement this to make sure the proctor session is valid: CommonDLL.ValidateProctorSession_FN
        String accessDeniedMessage = testSessionDao.validateProctorSession(testSession);

        if (accessDeniedMessage != null) {
            legacyErrorHandlerService.logDbError("P_PauseSession", accessDeniedMessage, testSession.getProctorId(), null, null, testSession.getKey());
            legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "P_PauseSession", accessDeniedMessage, null, null, "ValidateProctorSession", "failed");

            return;
        }

        // Lines 1778-1784 calls the DB to make sure the session key exists, but since we are passing in a TestSession, we are skipping that logic here

        // Lines 1785: Call to update the session in the DB
        // TODO: in the legacy code, the reason is hard-coded to "closed" even though a reason can be passed in.  The audit log insert uses the actual variable.  We are fixing that bug here by using the actual reason passed in.
        testSessionDao.pause(testSession, reason); // TODO: enumerate reasons?  Add reason getter/setter to TestSession?

        if (configurationService.isFlagOn(testOpportunity.getClientName(), "sessions")) {
            testSessionDao.createAudit(new SessionAudit(
                    testSession.getKey(),
                    now,
                    reason,
                    HostNameHelper.getHostName(),
                    testSession.getSessionBrowser(),
                    "session"
            ));
        }

        if (configurationService.isFlagOn(testOpportunity.getClientName(), "opportunities")) {
            testOpportunityDao.createAudit(new TestOpportunityAudit(
                    testOpportunity.getKey(),
                    now,
                    "paused by session",
                    testSession.getKey(),
                    HostNameHelper.getHostName(),
                    "session"
            ));
        }

        List<TestOpportunity> opportunities = testOpportunityDao.getBySessionAndStatus(testSession.getKey(), "Opportunity", "inuse");

        for (TestOpportunity opportunity : opportunities) {
            legacyTestOpportunityService.setOpportunityStatus(opportunity, "paused");
        }

        dbLatencyService.logLatency("P_PauseSession_SP", dateUtility.getLocalDate(), null, testSession);
    }


}
