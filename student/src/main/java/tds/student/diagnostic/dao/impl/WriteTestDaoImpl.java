/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 * <p/>
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 * <p/>
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
package tds.student.diagnostic.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Repository;
import tds.dll.common.performance.utils.LegacyDbNameUtility;
import tds.student.diagnostic.dao.WriteTestDao;
import tds.student.diagnostic.exceptions.DiagnosticException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Repository
public class WriteTestDaoImpl implements WriteTestDao {

    private static final Logger logger = LoggerFactory.getLogger(WriteTestDaoImpl.class);

    @Autowired
    private LegacyDbNameUtility dbNameUtility;

    @Autowired
    DataSource dataSource;

    @Override
    public void writeSessionDatabase() throws DiagnosticException {
        final String clientname = "DIAGNOSTIC_TEST";
        final Integer numopps = 99;
        final Timestamp maxtime = new Timestamp(System.currentTimeMillis());

        final String insert = "INSERT INTO ${sessiondb}._maxtestopps (numopps,_time, clientname)\n" +
                "VALUES (:numopps, :maxtime, :clientname);";

        final String query = "SELECT count(*) FROM ${sessiondb}._maxtestopps\n" +
                "where numopps = :numopps\n" +
                "and _time = :maxtime\n" +
                "and clientname = :clientname;";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientname", clientname);
        parameters.put("numopps", numopps);
        parameters.put("maxtime", maxtime);

        writeDatabase(insert, query, parameters);
    }

    @Override
    public void writeItemBankDatabase() throws DiagnosticException {
        final Integer duration = 99;
        final String procname = "DIAGNOSTIC_TEST";
        final Timestamp time = new Timestamp(System.currentTimeMillis());

        final String insert = "INSERT INTO ${itembankdb}._dblatency\n" +
                "(duration, endtime, procname, starttime)\n" +
                "VALUES\n" +
                "(:duration, :time, :procname, :time);";

        final String query = "select count(*) from ${itembankdb}._dblatency\n" +
                "where duration = :duration\n" +
                "and endtime = :time\n" +
                "and procname = :procname\n" +
                "and starttime = :time;";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("duration", duration);
        parameters.put("procname", procname);
        parameters.put("time", time);

        writeDatabase(insert, query, parameters);
    }

    @Override
    public void writeConfigsDatabase() throws DiagnosticException {
        final String procname = "DIAGNOSTIC_TEST";
        final String errormessage = "DIAGNOSTIC_TEST";
        final Timestamp daterecorded = new Timestamp(System.currentTimeMillis());

        final String insert = "INSERT INTO ${configdb}.systemerrors\n" +
                "(procname, errormessage, daterecorded )\n" +
                "VALUES\n" +
                "(:procname, :errormessage, :daterecorded );";

        final String query = "SELECT COUNT(*) FROM ${configdb}.systemerrors\n" +
                "WHERE procname = :procname\n" +
                "AND errormessage = :errormessage\n" +
                "AND daterecorded = :daterecorded ;";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("procname", procname);
        parameters.put("errormessage", errormessage);
        parameters.put("daterecorded", daterecorded);

        writeDatabase(insert, query, parameters);
    }


    @Override
    public void writeArchiveDatabase() throws DiagnosticException {
        final String clientname = "DIAGNOSTIC_TEST";
        final String procname = "DIAGNOSTIC_TEST";
        final Timestamp daterecorded = new Timestamp(System.currentTimeMillis());

        final String insert = "INSERT INTO ${archivedb}._dblatencyreports\n" +
                "(client, procname, daterecorded)\n" +
                "VALUES\n" +
                "(:clientname, :procname, :daterecorded );\n";

        final String query = "SELECT COUNT(*) FROM ${archivedb}._dblatencyreports\n" +
                "WHERE client = :clientname\n" +
                "AND procname = :procname\n" +
                "AND daterecorded = :daterecorded;";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientname", clientname);
        parameters.put("procname", procname);
        parameters.put("daterecorded", daterecorded);

        writeDatabase(insert, query, parameters);
    }



    private void writeDatabase(String insert, String query, Map<String, Object> parameters) throws DiagnosticException {
        Boolean readVerify = false;

        try {
            Connection connection = dataSource.getConnection();
            boolean originalAutoCommitMode = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                SingleConnectionDataSource singleDataSource = new SingleConnectionDataSource(connection, false);
                NamedParameterJdbcTemplate singleNamedTemplate = new NamedParameterJdbcTemplate(singleDataSource);

                singleNamedTemplate.update(dbNameUtility.setDatabaseNames(insert), parameters);

                Integer found = singleNamedTemplate.queryForInt(dbNameUtility.setDatabaseNames(query), parameters);
                logger.debug("Found count: {} for query {}", found, query);
                if (found == 1) {
                    readVerify = true;
                }

            } catch (DataAccessException dataAccessException) {
                logger.error("Exception during diagnostic write test", dataAccessException);
                throw new DiagnosticException("Error writing diagnostic test");

            } finally {
                connection.rollback();
                connection.setAutoCommit(originalAutoCommitMode);
                logger.debug("Completed diagnotstic write test with rollback.");
            }

        } catch (SQLException sqlException) {
            logger.error("Exception during diagnostic write test {}", sqlException);
            throw new DiagnosticException("Error in sql connection on writing diagnostic test", sqlException);
        }

        if (!readVerify) {
            throw new DiagnosticException("Read verify error on write test");
        }
    }


}
