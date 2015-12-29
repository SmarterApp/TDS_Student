package tds.student.performance.domain;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * Created by jjohnson on 12/24/15.
 */
public class TestOpportunityAudit {
    private UUID testOpportunityKey;
    private Timestamp dateAccessed;
    private String accessType;
    private UUID sessionKey;
    private Boolean isAbnormal;
    private String hostName;
    private UUID browserKey;
    private String comment;
    private String actor;
    private String databaseName;
    private String satellite;

    public TestOpportunityAudit() {}

    public TestOpportunityAudit(UUID testOpportunityKey, Timestamp dateAccessed, String accessType, UUID sessionKey, String hostName, String databaseName) {
        this.setTestOpportunityKey(testOpportunityKey);
        this.setDateAccessed(dateAccessed);
        this.setAccessType(accessType);
        this.setSessionKey(sessionKey);
        this.setHostName(hostName);
        this.setDatabaseName(databaseName);

    }

    public UUID getTestOpportunityKey() {
        return testOpportunityKey;
    }

    public void setTestOpportunityKey(UUID testOpportunityKey) {
        this.testOpportunityKey = testOpportunityKey;
    }

    public Timestamp getDateAccessed() {
        return dateAccessed;
    }

    public void setDateAccessed(Timestamp dateAccessed) {
        this.dateAccessed = dateAccessed;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public UUID getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(UUID sessionKey) {
        this.sessionKey = sessionKey;
    }

    public Boolean getAbnormal() {
        return isAbnormal;
    }

    public void setAbnormal(Boolean abnormal) {
        isAbnormal = abnormal;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public UUID getBrowserKey() {
        return browserKey;
    }

    public void setBrowserKey(UUID browserKey) {
        this.browserKey = browserKey;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getSatellite() {
        return satellite;
    }

    public void setSatellite(String satellite) {
        this.satellite = satellite;
    }
}
