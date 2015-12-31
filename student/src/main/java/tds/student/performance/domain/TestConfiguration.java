package tds.student.performance.domain;

/**
 * Created by jjohnson on 12/29/15.
 */
public class TestConfiguration {
    private Integer startPosition = 1;
    private String status;
    private Integer restart = 0;
    private Integer testLength;
    private String reason;
    private Integer interfaceTimeout;
    private Integer opportunityRestartDelay;
    private String excludeItemTypes;
    private Integer contentLoadTimeout = 120;
    private Integer requestInterfaceTimeout;
    private Integer prefetch;
    private Boolean validateCompleteness;
    private Boolean scoreByTds;

    public Integer getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Integer startPosition) {
        this.startPosition = startPosition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRestart() {
        return restart;
    }

    public void setRestart(Integer restart) {
        this.restart = restart;
    }

    public Integer getTestLength() {
        return testLength;
    }

    public void setTestLength(Integer testLength) {
        this.testLength = testLength;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getInterfaceTimeout() {
        return interfaceTimeout;
    }

    public void setInterfaceTimeout(Integer interfaceTimeout) {
        this.interfaceTimeout = interfaceTimeout;
    }

    public Integer getOpportunityRestartDelay() {
        return opportunityRestartDelay;
    }

    public void setOpportunityRestartDelay(Integer opportunityRestartDelay) {
        this.opportunityRestartDelay = opportunityRestartDelay;
    }

    public String getExcludeItemTypes() {
        return excludeItemTypes;
    }

    public void setExcludeItemTypes(String excludeItemTypes) {
        this.excludeItemTypes = excludeItemTypes;
    }

    public Integer getContentLoadTimeout() {
        return contentLoadTimeout;
    }

    public void setContentLoadTimeout(Integer contentLoadTimeout) {
        this.contentLoadTimeout = contentLoadTimeout;
    }

    public Integer getRequestInterfaceTimeout() {
        return requestInterfaceTimeout;
    }

    public void setRequestInterfaceTimeout(Integer requestInterfaceTimeout) {
        this.requestInterfaceTimeout = requestInterfaceTimeout;
    }

    public Integer getPrefetch() {
        return prefetch;
    }

    public void setPrefetch(Integer prefetch) {
        this.prefetch = prefetch;
    }

    public Boolean getValidateCompleteness() {
        return validateCompleteness;
    }

    public void setValidateCompleteness(Boolean validateCompleteness) {
        this.validateCompleteness = validateCompleteness;
    }

    public Boolean getScoreByTds() {
        return scoreByTds;
    }

    public void setScoreByTds(Boolean scoreByTds) {
        this.scoreByTds = scoreByTds;
    }
}
