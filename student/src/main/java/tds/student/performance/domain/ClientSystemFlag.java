package tds.student.performance.domain;

import java.util.Date;

/**
Represents a record in the {@code configs.client_systemflags} table.
 */
public class ClientSystemFlag {
    private String auditObject;
    private String clientName;
    private Boolean isPracticeTest;
    private Boolean isOn;
    private String description;
    private Date dateChanged;
    private Date datePublished;

    public ClientSystemFlag() { }

    public ClientSystemFlag(String auditObject, String clientName) {
        this.setAuditObject(auditObject);
        this.setClientName(clientName);
    }

    public String getAuditObject() {
        return auditObject;
    }

    public void setAuditObject(String auditObject) {
        this.auditObject = auditObject;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Boolean getIsPracticeTest() {
        return isPracticeTest;
    }

    public void setIsPracticeTest(Boolean practiceTest) {
        isPracticeTest = practiceTest;
    }

    public Boolean getIsOn() {
        return isOn;
    }

    public void setIsOn(Boolean on) {
        isOn = on;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }

    public Date getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(Date datePublished) {
        this.datePublished = datePublished;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (!(other instanceof ClientSystemFlag)) {
            return false;
        }

        if (this == other) {
            return true;
        }

        ClientSystemFlag that = (ClientSystemFlag)other;
        return this.getAuditObject().equals(that.getAuditObject())
                && this.getClientName().equals(that.getClientName());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(this);
    }
}
