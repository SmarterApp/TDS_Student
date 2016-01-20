package tds.student.performance.domain;

import java.util.UUID;

/**
 * Represents a record from the {@code configs.client_testmode} table.
 */
public class ClientTestMode {
    private UUID key;
    private String clientName;
    private String testId;
    private String mode;
    private String algorithm;
    private Boolean formTideSelectable;
    private Boolean isSegmented;
    private Integer maxOpps;
    private Boolean requireRtsForm;
    private Boolean requireRtsFormWindow;
    private Boolean requireRtsFormIfExists;
    private Integer sessionType;
    private String testKey;

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Boolean getFormTideSelectable() {
        return formTideSelectable;
    }

    public void setFormTideSelectable(Boolean formTideSelectable) {
        this.formTideSelectable = formTideSelectable;
    }

    public Boolean getIsSegmented() {
        return isSegmented;
    }

    public void setIsSegmented(Boolean isSegmented) {
        this.isSegmented = isSegmented;
    }

    public Integer getMaxOpps() {
        return maxOpps;
    }

    public void setMaxOpps(Integer maxOpps) {
        this.maxOpps = maxOpps;
    }

    public Boolean getRequireRtsForm() {
        return requireRtsForm;
    }

    public void setRequireRtsForm(Boolean requireRtsForm) {
        this.requireRtsForm = requireRtsForm;
    }

    public Boolean getRequireRtsFormWindow() {
        return requireRtsFormWindow;
    }

    public void setRequireRtsFormWindow(Boolean requireRtsFormWindow) {
        this.requireRtsFormWindow = requireRtsFormWindow;
    }

    /**
     * Referred to as {@code ifRts} in the query that fetches record(s) from {@code configs.client_testmode} from
     * {@code StudentDLL._SelectTestForm_Driver_SP}.
     */
    public Boolean getRequireRtsFormIfExists() {
        return requireRtsFormIfExists;
    }

    /**
     * Referred to as {@code ifRts} in the query that fetches record(s) from {@code configs.client_testmode} from
     * {@code StudentDLL._SelectTestForm_Driver_SP}.
     */
    public void setRequireRtsFormIfExists(Boolean requireRtsFormIfExists) {
        this.requireRtsFormIfExists = requireRtsFormIfExists;
    }

    public Integer getSessionType() {
        return sessionType;
    }

    public void setSessionType(Integer sessionType) {
        this.sessionType = sessionType;
    }

    public String getTestKey() {
        return testKey;
    }

    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }
}
