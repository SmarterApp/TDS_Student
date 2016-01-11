package tds.student.performance.domain;

import java.sql.Timestamp;

/**
 * Represents a record returned by the legacy {@code StudentDLL.GetTestFormWindows_FN} method.
 */
public class TestFormWindow {
    private String windowId;
    private Integer windowMax;
    private Integer modeMax;
    private Timestamp startDate;
    private Timestamp endDate;
    private Timestamp formStart;
    private Timestamp formEnd;
    private String formKey;
    private String formId;
    private String language;
    private String mode;
    private String testKey;
    private Integer windowSession;
    private Integer modeSession;

    public String getWindowId() {
        return windowId;
    }

    public void setWindowId(String windowId) {
        this.windowId = windowId;
    }

    public Integer getWindowMax() {
        return windowMax;
    }

    public void setWindowMax(Integer windowMax) {
        this.windowMax = windowMax;
    }

    public Integer getModeMax() {
        return modeMax;
    }

    public void setModeMax(Integer modeMax) {
        this.modeMax = modeMax;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public Timestamp getFormStart() {
        return formStart;
    }

    public void setFormStart(Timestamp formStart) {
        this.formStart = formStart;
    }

    public Timestamp getFormEnd() {
        return formEnd;
    }

    public void setFormEnd(Timestamp formEnd) {
        this.formEnd = formEnd;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTestKey() {
        return testKey;
    }

    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }

    public Integer getWindowSession() {
        return windowSession;
    }

    public void setWindowSession(Integer windowSession) {
        this.windowSession = windowSession;
    }

    public Integer getModeSession() {
        return modeSession;
    }

    public void setModeSession(Integer modeSession) {
        this.modeSession = modeSession;
    }
}
