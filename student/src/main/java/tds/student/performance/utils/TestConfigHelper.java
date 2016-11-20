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
package tds.student.performance.utils;

import TDS.Shared.Data.ReturnStatus;
import tds.student.performance.domain.ClientTestProperty;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSessionTimeLimitConfiguration;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.TestConfig;

/**
 * Build a legacy {@link TestConfig} from the acquired configuration data based on whether it is for a new opportunity
 * (i.e. an opportunity that has never been started) or an opportunity that has been started at least once before.
 * <p>
 *     The {@link TestConfig} class represents the results of the queries executed within
 *     {@code StudentDLL.T_StartTestOpportunity} on line 5342 (for a new opportunity) and/or line 5412 (for an
 *     opportunity that has been started at least once before).
 * </p>
 * <p>
 *     Looking at {@code OpportunityRepository.startTestOpportunity}, there is a section that maps the resulting
 *     recordset to a {@code TestConfig} object.  The following items are queried for in the database but are never
 *     mapped to the {@code TestConfig} that is returned by {@code OpportunityRepository.startTestOpportunity}:
 *
 *     * excludeItemTypes - this variable is defined at the beginning of the {@code StudentDLL.T_StartTestOpportunity_SP}
 *       method and set to null.  The variable is never updated to any other value.
 *
 *     * segmentsN - this value is queried from the database when fetching the rest of the data to populate the
 *     {@code TestConfig} object, but is not mapped in {@code OpportunityRepository.startTestOpportunity} prior to
 *     returning the {@code TestConfig} to the caller.
 * </p>
 */
public class TestConfigHelper {
    private static final Integer CONTENT_LOAD_TIMEOUT = 120;
    private static final String STARTED_STATUS = "started";

    /**
     * Get a new {@link TestConfig} for a {@link TestOpportunity} that has never been started before.
     *
     * @param clientTestProperty A {@link ClientTestProperty} for the {@link TestOpportunity}'s client name and test id.
     * @param timeLimitConfiguration A {@link TestSessionTimeLimitConfiguration} for the {@link TestOpportunity}'s
     *                               client name and test id.
     * @param testLength An {@link Integer} representing the number of items.
     * @return A {@link TestConfig} representing configuration for a new {@link TestOpportunity}.
     */
    public static TestConfig getNew(
            ClientTestProperty clientTestProperty,
            TestSessionTimeLimitConfiguration timeLimitConfiguration,
            Integer testLength,
            Boolean scoreByTds,
            Boolean isMsb) {

        TestConfig config = new TestConfig();
        config.setContentLoadTimeout(CONTENT_LOAD_TIMEOUT);
        config.setInterfaceTimeout(timeLimitConfiguration.getInterfaceTimeoutMinutes());
        config.setOppRestartMins(timeLimitConfiguration.getOpportunityRestartWindowMinutes());
        config.setPrefetch(clientTestProperty.getPrefetch());
        config.setRequestInterfaceTimeout(timeLimitConfiguration.getRequestInterfaceTimeoutMinutes());
        config.setRestart(0);
        config.setScoreByTDS(scoreByTds);
        config.setStartPosition(1);
        config.setStatus(OpportunityStatusType.parse(STARTED_STATUS));
        config.setTestLength(testLength);
        config.setValidateCompleteness(clientTestProperty.getValidateCompleteness());
        config.setIsMsb(isMsb);

        ReturnStatus startStatus = new ReturnStatus();
        startStatus.setStatus(STARTED_STATUS);
        config.setReturnStatus(startStatus);

        return config;
    }

    /**
     * Get a new {@link TestConfig} for a {@link TestOpportunity} that has never been started before.
     *
     * @param clientTestProperty A {@link ClientTestProperty} for the {@link TestOpportunity}'s client name and test id.
     * @param timeLimitConfiguration A {@link TestSessionTimeLimitConfiguration} for the {@link TestOpportunity}'s
     *                               client name and test id.
     * @param testLength An {@link Integer} representing the number of items.
     * @param restartCount An {@link Integer} represnting the current restart iteration.
     * @param restartPosition An {@link Integer} representing the first question the restart should display.
     * @return A {@link TestConfig} representing configuration for a new {@link TestOpportunity}.
     */
    public static TestConfig getRestart(
            ClientTestProperty clientTestProperty,
            TestSessionTimeLimitConfiguration timeLimitConfiguration,
            Integer testLength,
            Integer restartCount,
            Integer restartPosition,
            Boolean scoreByTds,
            Boolean isMsb) {
        TestConfig config = new TestConfig();
        config.setContentLoadTimeout(CONTENT_LOAD_TIMEOUT);
        config.setInterfaceTimeout(timeLimitConfiguration.getInterfaceTimeoutMinutes());
        config.setOppRestartMins(timeLimitConfiguration.getOpportunityRestartWindowMinutes());
        config.setPrefetch(clientTestProperty.getPrefetch());
        config.setRequestInterfaceTimeout(timeLimitConfiguration.getRequestInterfaceTimeoutMinutes());
        config.setRestart(restartCount);
        config.setScoreByTDS(scoreByTds);
        config.setStartPosition(restartPosition);
        config.setStatus(OpportunityStatusType.parse(STARTED_STATUS));
        config.setTestLength(testLength);
        config.setValidateCompleteness(clientTestProperty.getValidateCompleteness());
        config.setIsMsb(isMsb);

        ReturnStatus startStatus = new ReturnStatus();
        startStatus.setStatus(STARTED_STATUS);
        config.setReturnStatus(startStatus);

        return config;
    }
}
