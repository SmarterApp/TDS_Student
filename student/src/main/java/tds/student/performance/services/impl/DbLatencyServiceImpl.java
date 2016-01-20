package tds.student.performance.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tds.student.performance.dao.DbLatencyDao;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSession;
import tds.student.performance.services.DbLatencyService;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Service
public class DbLatencyServiceImpl implements DbLatencyService {
    private static final Logger logger = LoggerFactory.getLogger(DbLatencyServiceImpl.class);

    @Autowired
    DbLatencyDao dbLatencyDao;

    @Value("${logLatencyInterval:59}")
    private Integer logLatencyInterval;

    @Value ("${logLatencyMaxTime:30000}")
    private Integer logLatencyMaxTime;

    @Value("${performance.logLatency.enabled:false}")
    private Boolean logLatencyEnabled;

    public void setEnabled(Boolean value) {
        logLatencyEnabled = value;
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, Integer n, TestOpportunity testOpportunity, String comment) {
        logLatency(procname, startTime, userKey, n, testOpportunity.getKey(), testOpportunity.getSessionKey(), testOpportunity.getClientName(), comment);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, Integer n, TestSession testSession, String comment) {
        logLatency(procname, startTime, userKey, n, null, testSession.getKey(), testSession.getClientName(), comment);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, Integer n, String clientName, String comment) {
        logLatency(procname, startTime, userKey, n, null, null, clientName, comment);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, TestOpportunity testOpportunity, String comment) {
        logLatency(procname, startTime, userKey, null, testOpportunity, comment);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, TestSession testSession, String comment) {
        logLatency(procname, startTime, userKey, null, testSession, comment);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, String clientName, String comment) {
        logLatency(procname, startTime, userKey, null, clientName, comment);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, TestOpportunity testOpportunity) {
        logLatency(procname, startTime, userKey, null, testOpportunity, null);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, TestSession testSession) {
        logLatency(procname, startTime, userKey, null, testSession, null);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, String clientName) {
        logLatency(procname, startTime, userKey, null, clientName, null);
    }

    // PORT of CommonDLL._LogDBLatency_SP line 2289
    public void logLatency(String procName, Date startTime, Long userKey, Integer n, UUID testoppKey, UUID sessionKey, String clientName, String comment) {
        if (logLatencyEnabled == null || !logLatencyEnabled) {
            return;
        }

        boolean logDBLatency = false;
        Date now = new Date();
        long duration = now.getTime() - startTime.getTime();
        if (duration < 0) {
            duration = 0;
        }

        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now);
        int currSeconds = nowCal.get(Calendar.SECOND);
        if (currSeconds % logLatencyInterval == 0 || duration > logLatencyMaxTime) {
            logDBLatency = true;
        }

        if (!logDBLatency) {
            return;
        }

        // TODO: lines 2314-2332 look up the client name if a test opportunity key or test session key is provided but the client name is not
        //  i've made the assumption currently that we will always pass in the domain object and use it to get the client name instead of hitting the DB
        //  or where that is not applicable, the client name will be passed in directly
        //  That is why this is a private helper method and not part of the interface API directly

        Date diffTime = new Date(duration);

        try {
            dbLatencyDao.create(procName, duration, startTime, diffTime, userKey, n, testoppKey, sessionKey, clientName, comment);
        } catch(Exception e) {
            logger.error(String.format("Error logging DB latency.  Swallowing it since it is not critical."), e);
        }

    }
}
