package tds.student.performance.domain;

import tds.student.sql.data.OpportunityInstance;

import java.util.Date;
import java.util.UUID;

/**
Represents a single record in the {@code session.testopportunity} database.
 */
public class TestOpportunity {
    private UUID key;
    private UUID sessionKey;
    private UUID browserKey;
    private String testKey;
    private Double testee;
    private String testId;
    private String adminSubject;
    private Integer opportunity;
    private String status;
    private Date dateStarted;
    private Date dateChanged;
    private Integer restartCount;
    private Integer gracePeriodRestarts;
    private Integer maxItems;
    private String subject;
    private String clientName;
    private Boolean isSegmented;
    private String algorithm;

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public UUID getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(UUID sessionKey) {
        this.sessionKey = sessionKey;
    }

    public UUID getBrowserKey() {
        return browserKey;
    }

    public void setBrowserKey(UUID browserKey) {
        this.browserKey = browserKey;
    }

    public String getTestKey() {
        return testKey;
    }

    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }

    public Double getTestee() {
        return testee;
    }

    public void setTestee(Double testee) {
        this.testee = testee;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    /**
     * Referred to as {@code testkey} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @return {@code String } representing the test subject key of the {@code TestOpportunity}.
     */
    public String getAdminSubject() {
        return adminSubject;
    }

    /**
     * Referred to as {@code testkey} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @param adminSubject  {@code String } representing the test subject key of the {@code TestOpportunity}.
     */
    public void setAdminSubject(String adminSubject) {
        this.adminSubject = adminSubject;
    }

    public Integer getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Integer opportunity) {
        this.opportunity = opportunity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    public Date getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }

    /**
     * Referred to as {@code rcnt} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @return {@code Integer } representing the restart count of the {@code TestOpportunity}.
     */
    public Integer getRestartCount() {
        return restartCount;
    }

    /**
     * Referred to as {@code rcnt} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @param restartCount {@code Integer } representing the restart count of the {@code TestOpportunity}.
     */
    public void setRestartCount(Integer restartCount) {
        this.restartCount = restartCount;
    }

    /**
     * Referred to as {@code gpRestarts} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @return {@code Integer } representing the restart count of the {@code TestOpportunity}.
     */
    public Integer getGracePeriodRestarts() {
        return gracePeriodRestarts;
    }

    public void setGracePeriodRestarts(Integer gracePeriodRestarts) {
        this.gracePeriodRestarts = gracePeriodRestarts;
    }

    /**
     * Referred to as {@code testlength} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @return {@code Integer } representing the maximum number of items in the {@code TestOpportunity}.
     */
    public Integer getMaxItems() {
        return maxItems;
    }

    /**
     * Referred to as {@code testlength} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @param maxItems  {@code Integer } representing the maximum number of items in the {@code TestOpportunity}.
     */
    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Boolean getIsSegmented() {
        return isSegmented;
    }

    public void setIsSegmented(Boolean isSegmented) {
        this.isSegmented = isSegmented;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
