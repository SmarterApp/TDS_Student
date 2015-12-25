package tds.student.performance.domain;

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
    private String status;
    private Date dateBegin;
    private Date dateEnd;
    private Date dateVisited;
    private String clientName;
    private Double proctorId;
    private UUID sessionBrowser;

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateBegin() {
        return dateBegin;
    }

    public void setDateBegin(Date dateBegin) {
        this.dateBegin = dateBegin;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public Date getDateVisited() {
        return dateVisited;
    }

    public void setDateVisited(Date dateVisited) {
        this.dateVisited = dateVisited;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Double getProctorId() {
        return proctorId;
    }

    public void setProctorId(Double proctorId) {
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
}
