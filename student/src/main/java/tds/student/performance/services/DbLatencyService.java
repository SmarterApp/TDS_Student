package tds.student.performance.services;

import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSession;

import java.util.Date;
import java.util.UUID;

public interface DbLatencyService {
    void setEnabled(Boolean value);

    void logLatency(String procname, Date startTime, Long userKey, Integer n, TestOpportunity testOpportunity, String comment);
    void logLatency(String procname, Date startTime, Long userKey, Integer n, TestSession testSession, String comment);
    void logLatency(String procname, Date startTime, Long userKey, Integer n, String clientName, String comment);

    void logLatency(String procname, Date startTime, Long userKey, TestOpportunity testOpportunity, String comment);
    void logLatency(String procname, Date startTime, Long userKey, TestSession testSession, String comment);
    void logLatency(String procname, Date startTime, Long userKey, String clientName, String comment);

    void logLatency(String procname, Date startTime, Long userKey, TestOpportunity testOpportunity);
    void logLatency(String procname, Date startTime, Long userKey, TestSession testSession);
    void logLatency(String procname, Date startTime, Long userKey, String clientName);

    void logLatency(String procname, Date startTime, Long userKey, Integer n, UUID testoppKey, UUID sessionKey, String clientName, String comment);
}
