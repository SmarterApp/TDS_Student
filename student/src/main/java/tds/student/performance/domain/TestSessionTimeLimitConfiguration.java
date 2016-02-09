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
package tds.student.performance.domain;

import java.sql.Timestamp;

/**
 * Represents a record from the {@code session.timelimits} view.
 */
public class TestSessionTimeLimitConfiguration {
    private String testId;
    private Integer opportunityExpiration;
    private Integer opportunityRestartMinutes;
    private Integer opportunityDelayDays;
    private Integer interfaceTimeoutMinutes;
    private Integer requestInterfaceTimeoutMinutes;
    private String clientName;
    private String environment;
    private Boolean isPracticeTest;
    private Integer refreshValue;
    private Integer taInterfaceTimeout;
    private Integer taCheckinTimeMinutes;
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
     * Referred to as "delay" in {@code StudentDLL.T_StartTestOpportunity_SP} @ line 5310.
     * <p>
     *     This value is in minutes.
     * </p>
     */
    public Integer getOpportunityRestartWindowMinutes() {
        return opportunityRestartMinutes;
    }

    /**
     * Referred to as "delay" in {@code StudentDLL.T_StartTestOpportunity_SP} @ line 5310.
     * <p>
     *     Logic @ line 3689 in {@code StudentDLL.T_StartTestOpportunity_SP} states that if
     *     {@code session.timeouts.opprestart} is null, it should be set to 1.
     * </p>
     * <p>
     *     This value is in minutes.
     * </p>
     *
     * @param opportunityRestart the value that {@code opportunityRestartMinutes} should be set to.
     */
    public void setOpportunityRestartMinutes(Integer opportunityRestart) {
        this.opportunityRestartMinutes = opportunityRestart == null
            ? 1
            : opportunityRestart;
    }

    /**
     * The number of days that must pass before a student can resume/restart the same opportunity..
     * <p>
     *     This value is expressed in days.
     * </p>
     *
     * @return The number of days before an opportunity can be retaken.
     */
    public Integer getOpportunityDelayDays() {
        return opportunityDelayDays;
    }

    /**
     * The number of days that must pass before a student can resume/restart the same opportunity.
     * <p>
     *     This value is expressed in days.
     * </p>
     *
     * @param opportunityDelay  The number of days before an opportunity can be retaken.
     */
    public void setOpportunityDelayDays(Integer opportunityDelay) {
        this.opportunityDelayDays = opportunityDelay;
    }

    /**
     * This value is in minutes.  This definition came from the {@link tds.student.sql.data.TestConfig} getter.
     *
     * @return The number of minutes a student can be idle before logging them out.
     */
    public Integer getInterfaceTimeoutMinutes() {
        return interfaceTimeoutMinutes;
    }

    /**
     * The number of minutes a student can be idle before logging them out.
     * <p>
     *     This value is in minutes.  This definition came from the {@link tds.student.sql.data.TestConfig} getter.
     * </p>
     */
    public void setInterfaceTimeoutMinutes(Integer interfaceTimeout) {
        this.interfaceTimeoutMinutes = interfaceTimeout;
    }

    /**
     * This value is in minutes.  This definition came from the {@link tds.student.sql.data.TestConfig} getter.
     *
     * @return The number of minutes a student can be idle after making a print request before logging them out.
     */
    public Integer getRequestInterfaceTimeoutMinutes() {
        return requestInterfaceTimeoutMinutes;
    }

    /**
     * The number of minutes a student can be idle after making a print request before logging them out.
     * <p>
     *     This value is in minutes.  This definition came from the {@link tds.student.sql.data.TestConfig} getter.
     * </p>
     */
    public void setRequestInterfaceTimeoutMinutes(Integer requestInterfaceTimeout) {
        this.requestInterfaceTimeoutMinutes = requestInterfaceTimeout;
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

    /**
     * This value is in minutes.
     *
     * @return The number of minutes before the TA CheckIn time expires.
     */
    public Integer getTaCheckinTimeMinutes() {
        return taCheckinTimeMinutes;
    }

    /**
     * This value is in minutes.
     *
     * @return The number of minutes before the TA CheckIn time expires.
     */
    public void setTaCheckinTimeMinutes(Integer taCheckinTimeMinutes) {
        this.taCheckinTimeMinutes = taCheckinTimeMinutes;
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
}
