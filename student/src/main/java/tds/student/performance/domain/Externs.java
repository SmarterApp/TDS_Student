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

/**
 * Holds data from the session.externs VIEW
 */
public class Externs {
    private String clientName;
    private String testeeType;
    private String proctorType;
    private String environment;
    private Boolean isPracticeTest;
    private String sessionDb;
    private String testDb;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getTesteeType() {
        return testeeType;
    }

    public void setTesteeType(String testeeType) {
        this.testeeType = testeeType;
    }

    public String getProctorType() {
        return proctorType;
    }

    public void setProctorType(String proctorType) {
        this.proctorType = proctorType;
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

    public void setIsPracticeTest(Boolean practiceTest) {
        isPracticeTest = practiceTest;
    }

    public String getSessionDb() {
        return sessionDb;
    }

    public void setSessionDb(String sessionDb) {
        this.sessionDb = sessionDb;
    }

    public String getTestDb() {
        return testDb;
    }

    public void setTestDb(String testDb) {
        this.testDb = testDb;
    }
}
