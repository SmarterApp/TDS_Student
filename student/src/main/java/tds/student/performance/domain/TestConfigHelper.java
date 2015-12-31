package tds.student.performance.domain;

import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.TestConfig;

/**
 * Build a {@link TestConfiguration} from the acquired configuration data based on whether it is for a new opportunity
 * (i.e. an opportunity that has never been started) or an opportunity that has been started at least once before.  The
 * {@link TestConfiguration} will be mapped to a legacy {@link TestConfig} object that can be consumed by the legacy
 * caller.
 * <p>
 *     The {@link TestConfiguration} class represents the results of the queries executed within
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

    /**
     * Get a new {@link TestConfiguration} for a {@link TestOpportunity} that has never been started before.
     *
     * @param clientTestProperty A {@link ClientTestProperty} for the {@link TestOpportunity}'s client name and test id.
     * @param timelimitConfiguration A {@link TestSessionTimeLimitConfiguration} for the {@link TestOpportunity}'s
     *                               client name and test id.
     * @param testLength An {@link Integer} representing the number of items.
     * @return A {@link TestConfig} representing configuration for a new {@link TestOpportunity}.
     */
    public static TestConfig getNew(
            ClientTestProperty clientTestProperty,
            TestSessionTimeLimitConfiguration timelimitConfiguration,
            Integer testLength,
            Boolean scoreByTds) {
        TestConfiguration configuration = new TestConfiguration();
        configuration.setStatus("started");
        configuration.setTestLength(testLength);
        configuration.setInterfaceTimeout(timelimitConfiguration.getInterfaceTimeout());
        configuration.setOpportunityRestartDelay(timelimitConfiguration.getOpportunityDelay());
        configuration.setRequestInterfaceTimeout(timelimitConfiguration.getRequestInterfaceTimeout());
        configuration.setPrefetch(clientTestProperty.getPrefetch());
        configuration.setValidateCompleteness(clientTestProperty.getValidateCompleteness());
        configuration.setScoreByTds(scoreByTds);

        return map(configuration);
    }

    /**
     * Get a new {@link TestConfiguration} for a {@link TestOpportunity} that has never been started before.
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
            Boolean scoreByTds) {
        TestConfiguration configuration = new TestConfiguration();
        configuration.setStatus("started");
        configuration.setRestart(restartCount);
        configuration.setTestLength(testLength);
        configuration.setInterfaceTimeout(timeLimitConfiguration.getInterfaceTimeout());
        configuration.setOpportunityRestartDelay(timeLimitConfiguration.getOpportunityDelay());
        configuration.setRequestInterfaceTimeout(timeLimitConfiguration.getRequestInterfaceTimeout());
        configuration.setPrefetch(clientTestProperty.getPrefetch());
        configuration.setValidateCompleteness(clientTestProperty.getValidateCompleteness());
        configuration.setStartPosition(restartPosition);
        configuration.setScoreByTds(scoreByTds);

        return map(configuration);
    }

    /**
     * Map a {@link TestConfiguration} to a {@code TestConfig} for the legacy code to consume.
     *
     * @param testConfiguration A {@link TestConfiguration} containing data the {@link TestConfig} needs.
     * @return A {@link TestConfig} containing configuration necessary for the {@link TestOpportunity} to start or
     * restart.
     */
    private static TestConfig map(TestConfiguration testConfiguration) {
        TestConfig config = new TestConfig();
        config.setContentLoadTimeout(testConfiguration.getContentLoadTimeout());
        config.setInterfaceTimeout(testConfiguration.getInterfaceTimeout());
        config.setOppRestartMins(testConfiguration.getOpportunityRestartDelay());
        config.setPrefetch(testConfiguration.getPrefetch());
        config.setRequestInterfaceTimeout(testConfiguration.getRequestInterfaceTimeout());
        config.setRestart(testConfiguration.getRestart());
        config.setScoreByTDS(testConfiguration.getScoreByTds());
        config.setStartPosition(testConfiguration.getStartPosition());
        config.setStatus(OpportunityStatusType.parse(testConfiguration.getStatus()));
        config.setTestLength(testConfiguration.getTestLength());
        config.setValidateCompleteness(testConfiguration.getValidateCompleteness());

        return config;
    }
}
