package tds.student.performance.domain;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Represents a record from the {@code session.timelimits} view.
 */
public class TestSessionTimeLimitConfiguration {
    private String testId;
    private Integer opportunityExpiration;
    private Integer opportunityRestart;
    private Integer opportunityDelay;
    private Integer interfaceTimeout;
    private Integer requestInterfaceTimeout;
    private String clientName;
    private String environment;
    private Boolean isPracticeTest;
    private Integer refreshValue;
    private Integer taInterfaceTimeout;
    private Integer taCheckinTime;
    private Timestamp dateChanged;
    private Timestamp datePublished;
    private Integer sessionExpiration;
    private Integer refreshValueMultiplier;

    public TestSessionTimeLimitConfiguration() { }

    public TestSessionTimeLimitConfiguration(String clientName, String testId) {
        this.setClientName(clientName);
        this.setTestId(testId);
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public Integer getOpportunityExpiration() {
        return opportunityExpiration;
    }

    public void setOpportunityExpiration(Integer opportunityExpire) {
        this.opportunityExpiration = opportunityExpire;
    }

    /**
     * Referred to as "delay" in {@code StudentDLL.T_StartTestOpportunity_SP} @ line 3668.
     */
    public Integer getOpportunityRestart() {
        return opportunityRestart;
    }

    /**
     * Referred to as "delay" in {@code StudentDLL.T_StartTestOpportunity_SP} @ line 3668.
     * <p>
     *     Logic @ line 3689 in {@code StudentDLL.T_StartTestOpportunity_SP} states that if
     *     {@code session.timeouts.opprestart} is null, it should be set to 1.
     * </p>
     * @param opportunityRestart the value that {@code opportunityRestart} should be set to.
     */
    public void setOpportunityRestart(Integer opportunityRestart) {
        this.opportunityRestart = opportunityRestart == null
            ? 1
            : opportunityRestart;
    }

    public Integer getOpportunityDelay() {
        return opportunityDelay;
    }

    public void setOpportunityDelay(Integer opportunityDelay) {
        this.opportunityDelay = opportunityDelay;
    }

    public Integer getInterfaceTimeout() {
        return interfaceTimeout;
    }

    public void setInterfaceTimeout(Integer interfaceTimeout) {
        this.interfaceTimeout = interfaceTimeout;
    }

    public Integer getRequestInterfaceTimeout() {
        return requestInterfaceTimeout;
    }

    public void setRequestInterfaceTimeout(Integer requestInterfaceTimeout) {
        this.requestInterfaceTimeout = requestInterfaceTimeout;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Boolean getIsPracticeTest() {
        return isPracticeTest;
    }

    public void setIsPracticeTest(Boolean isPracticeTest) {
        this.isPracticeTest = isPracticeTest;
    }

    public Integer getRefreshValue() {
        return refreshValue;
    }

    public void setRefreshValue(Integer refreshValue) {
        this.refreshValue = refreshValue;
    }

    public Integer getTaInterfaceTimeout() {
        return taInterfaceTimeout;
    }

    public void setTaInterfaceTimeout(Integer taInterfaceTimeout) {
        this.taInterfaceTimeout = taInterfaceTimeout;
    }

    public Integer getTaCheckinTime() {
        return taCheckinTime;
    }

    public void setTaCheckinTime(Integer taCheckinTime) {
        this.taCheckinTime = taCheckinTime;
    }

    public Timestamp getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(Timestamp dateChanged) {
        this.dateChanged = dateChanged;
    }

    public Timestamp getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(Timestamp datePublished) {
        this.datePublished = datePublished;
    }

    public Integer getSessionExpiration() {
        return sessionExpiration;
    }

    public void setSessionExpiration(Integer sessionExpiration) {
        this.sessionExpiration = sessionExpiration;
    }

    public Integer getRefreshValueMultiplier() {
        return refreshValueMultiplier;
    }

    public void setRefreshValueMultiplier(Integer refreshValueMultiplier) {
        this.refreshValueMultiplier = refreshValueMultiplier;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (!(other instanceof TestSessionTimeLimitConfiguration)) {
            return false;
        }

        if (this == other) {
            return true;
        }

        TestSessionTimeLimitConfiguration that = (TestSessionTimeLimitConfiguration)other;

        return this.testId.equals(that.testId)
                && this.clientName.equals(that.clientName)
                && this.environment.equals(that.environment);
    }
}
