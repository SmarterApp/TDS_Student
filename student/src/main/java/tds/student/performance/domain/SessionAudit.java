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
 * Represents a single record in the {@code session.sessionaudit} table.
 */
public class SessionAudit {
    private UUID sessionKey;
    private Timestamp dateAccessed;
    private String accessType;
    private String hostName;
    private UUID browserKey;
    private String databaseName;

    public SessionAudit() {}

    public SessionAudit(UUID sessionKey, Timestamp dateAccessed, String accessType, String hostName, UUID browserKey, String databaseName) {
        this.setSessionKey(sessionKey);
        this.setDateAccessed(dateAccessed);
        this.setAccessType(accessType);
        this.setHostName(hostName);
        this.setBrowserKey(browserKey);
        this.setDatabaseName(databaseName);
    }

    public UUID getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(UUID sessionKey) {
        this.sessionKey = sessionKey;
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

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}