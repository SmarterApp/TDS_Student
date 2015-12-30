package tds.student.performance.domain;

/**
 * Build a {@link TestConfiguration} from the acquired configuration data based on whether it is for a new opportunity
 * (i.e. an opportunity that has never been started) or an opportunity that has been started at least once before.
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
public class TestConfigurationFactory {
    /**
     * Get a new {@link TestConfiguration} for a {@link TestOpportunity} that has never been started before.
     * @param clientTestProperty A {@link ClientTestProperty} for the {@link TestOpportunity}'s client name and test id.
     * @param timelimitConfiguration A {@link TestSessionTimeLimitConfiguration} for the {@link TestOpportunity}'s client name and test id.
     * @param testLength An {@link Integer} representing the number of items.
     * @return A {@link TestConfiguration} representing configuration for a new {@link TestOpportunity}.
     */
    public static TestConfiguration getNew(ClientTestProperty clientTestProperty, TestSessionTimeLimitConfiguration timelimitConfiguration, Integer testLength) {
        TestConfiguration configuration = new TestConfiguration();
        configuration.setStatus("started");
        configuration.setTestLength(testLength);
        configuration.setInterfaceTimeout(timelimitConfiguration.getInterfaceTimeout()); // need to double-check this
        configuration.setOpportunityRestart(timelimitConfiguration.getOpportunityDelay());
        configuration.setRequestInterfaceTimeout(timelimitConfiguration.getRequestInterfaceTimeout());
        configuration.setPrefetch(clientTestProperty.getPrefetch());
        configuration.setValidateCompleteness(clientTestProperty.getValidateCompleteness());

        return configuration;
    }

    // TODO:  Construct a TestConfiguration for a restarted TestOpportunity.
    public static TestConfiguration getRestart() {
        return null;
    }
}
