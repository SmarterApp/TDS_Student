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
