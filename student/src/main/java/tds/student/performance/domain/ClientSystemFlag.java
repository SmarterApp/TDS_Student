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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
Represents a record in the {@code configs.client_systemflags} table.
 */
public class ClientSystemFlag implements Serializable {
    private String auditObject = "";
    private String clientName = "";
    private Boolean isPracticeTest;
    private Boolean isOn;
    private String description;
    private Timestamp dateChanged;
    private Timestamp datePublished;

    public ClientSystemFlag() { }

    public ClientSystemFlag(String auditObject, String clientName) {
        this.setAuditObject(auditObject);
        this.setClientName(clientName);
    }

    public String getAuditObject() {
        return auditObject;
    }

    /**
     * Protect against a {@link NullPointerException} in the event {@code equals} is called.  Possible code smell; look
     * into refactoring {@code equals} method..
     */
    public void setAuditObject(String auditObject) {
        this.auditObject = auditObject == null
                ? ""
                : auditObject;
    }

    public String getClientName() {
        return clientName;
    }

    /**
     * Protect against a {@link NullPointerException} in the event {@code equals} is called.  Possible code smell; look
     * into refactoring {@code equals} method..
     */
    public void setClientName(String clientName) {
        this.clientName = clientName == null
                ? ""
                : clientName;
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
