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
package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.student.performance.dao.DbLatencyDao;
import tds.student.performance.utils.HostNameHelper;
import tds.student.performance.utils.LegacyDbNameUtility;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.*;

/**
 * Data Access Object for interacting with the {@code _dblatency} database.
 * <p>
 *     This class is called from the {@code DbLatencyService} to insert a new record into the archive._dblatency table.
 *     It can be disabled in production by setting the logLatency.enabled property to false.  This method won't get called then through the service.
 * </p>
 */
@Repository
public class DbLatencyDaoImpl implements DbLatencyDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(DbLatencyDaoImpl.class);

    @Autowired
    private LegacyDbNameUtility dbNameUtility;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public void create(String procName, Long duration, Date startTime, Date diffTime, Long userKey, Integer n, UUID testoppKey, UUID sessionKey, String clientName, String comment) {
        final String sessionDb = "session";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userKey", userKey);
        parameters.put("duration", duration);
        parameters.put("startTime", startTime);
        parameters.put("diffTime", diffTime);
        parameters.put("procName", procName);
        parameters.put("N", n);
        parameters.put("testoppKey", UuidAdapter.getBytesFromUUID(testoppKey));
        parameters.put("sessionKey", UuidAdapter.getBytesFromUUID(sessionKey));
        parameters.put("clientName", clientName);
        parameters.put("comment", comment);
        parameters.put("localhost", HostNameHelper.getHostName());
        parameters.put("dbName", sessionDb);

        final String SQL =
                "INSERT INTO\n" +
                        "${archivedb}._dblatency (" +
                        "userkey," +
                        "duration," +
                        "starttime," +
                        "difftime," +
                        "procname," +
                        "N," +
                        "_fk_TestOpportunity," +
                        "_fk_session," +
                        "clientname," +
                        "comment," +
                        "host," +
                        "dbname)\n" +
                        "VALUES(" +
                        ":userKey," +
                        ":duration," +
                        ":startTime," +
                        ":diffTime," +
                        ":procName," +
                        ":N," +
                        ":testoppKey," +
                        ":sessionKey," +
                        ":clientName," +
                        ":comment," +
                        ":localhost," +
                        ":dbName)";

        try {
            namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(SQL), parameters);
        } catch (DataAccessException e) {
            logger.error(String.format("%s INSERT threw exception", SQL), e);
            throw e;
        }
    }

}
