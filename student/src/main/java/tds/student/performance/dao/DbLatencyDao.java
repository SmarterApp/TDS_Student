package tds.student.performance.dao;

import java.util.Date;
import java.util.UUID;

/**
 * Created by jtreuting on 12/29/15.
 */
public interface DbLatencyDao {
    void create(String procName, Long duration, Date startTime, Date diffTime, Long userKey, Integer n, UUID testoppKey, UUID sessionKey, String clientName, String comment);
}
