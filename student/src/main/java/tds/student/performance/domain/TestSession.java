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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 select
 status as sessionStatus,
 DateBegin,
 DateEnd,
 dateVisited,
 clientname,
 _efk_Proctor as proctor,
 _fk_Browser as sessionBrowser
 from
 session
 where
 _Key =
 */
public class TestSession {
    private UUID key;
    private Integer sessionType;
    private String sessionId;
    private String proctorName;
    private String status;
    private Timestamp dateBegin;
    private Timestamp dateEnd;
    private Timestamp dateVisited;
    private String clientName;
    private Long proctorId;
    private UUID sessionBrowser;

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public Integer getSessionType() {
        return sessionType;
    }

    public void setSessionType(Integer sessionType) {
        this.sessionType = sessionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getDateBegin() {
        return dateBegin;
    }

    public void setDateBegin(Timestamp dateBegin) {
        this.dateBegin = dateBegin;
    }

    public Timestamp getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Timestamp dateEnd) {
        this.dateEnd = dateEnd;
    }

    public Timestamp getDateVisited() {
        return dateVisited;
    }

    public void setDateVisited(Timestamp dateVisited) {
        this.dateVisited = dateVisited;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Long getProctorId() {
        return proctorId;
    }

    public void setProctorId(Long proctorId) {
        this.proctorId = proctorId;
    }

    public UUID getSessionBrowser() {
        return sessionBrowser;
    }

    public void setSessionBrowser(UUID sessionBrowser) {
        this.sessionBrowser = sessionBrowser;
    }

    /**
     * Logic from line 523 of StudentDLL._ValidateTesteeAccessProc_SP:
     *
     * dateBegin = this._commonDll.adjustDateMinutes(dateBegin, Integer.valueOf(-5));
     * if(DbComparator.notEqual(sessionStatus, "open") || DbComparator.lessThan(now, dateBegin) || DbComparator.greaterThan(now, dateEnd)) {
     *  message.set("The session is not available for testing, please check with your test administrator.");
     *  return;
     * }
     * @param now The date and time being checked.
     * @return {@code True} if the {@code TestSession}'s status is "open" and the time passed in is before the date and
     * time when the {@code TestSession} ends; otherwise {@code False};
     */
    public Boolean isOpen(Date now) {
        final String OPEN_STATUS = "open";
        final Long FIVE_MINUTES_IN_MILLISECONDS = 300000l;

        Date fiveMinutesAgo = new Date(now.getTime() - FIVE_MINUTES_IN_MILLISECONDS);

        return this.status.toLowerCase().equals(OPEN_STATUS)
                && now.after(fiveMinutesAgo)
                && now.before(this.dateEnd);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getProctorName() {
        return proctorName;
    }

    public void setProctorName(String proctorName) {
        this.proctorName = proctorName;
    }
}
