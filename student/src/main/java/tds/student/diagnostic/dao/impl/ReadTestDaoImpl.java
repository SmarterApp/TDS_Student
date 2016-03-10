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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.student.diagnostic.dao.ReadTestDao;
import tds.student.diagnostic.exceptions.DiagnosticException;
import tds.student.performance.utils.LegacyDbNameUtility;

import javax.sql.DataSource;

@Repository
public class ReadTestDaoImpl implements ReadTestDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ReadTestDaoImpl.class);

    @Autowired
    private LegacyDbNameUtility dbNameUtility;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void readSessionDatabase() throws DiagnosticException {

        final String query = "SELECT \n" +
                "    COUNT(*)\n" +
                "FROM\n" +
                "    ${sessiondb}.client_sessionid;";
        readDatabase(query);
    }

    @Override
    public void readConfigsDatabase() throws DiagnosticException {

        final String query = "SELECT \n" +
                "    COUNT(*)\n" +
                "FROM\n" +
                "    ${configdb}.systemerrors;";
        readDatabase(query);
    }

    @Override
    public void readItemBankDatabase() throws DiagnosticException {

        final String query = "SELECT \n" +
                "    COUNT(*)\n" +
                "FROM\n" +
                "    ${itembankdb}._dblatency;";
        readDatabase(query);
    }

    @Override
    public void readArchiveDatabase() throws DiagnosticException {

        final String query = "SELECT \n" +
                "    COUNT(*)\n" +
                "FROM\n" +
                "    ${archivedb}._dblatencyreports;";
        readDatabase(query);
    }

    private void readDatabase(String query) throws DiagnosticException {
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        try {
            namedParameterJdbcTemplate.queryForObject(
                    dbNameUtility.setDatabaseNames(query),
                    parameters,
                    Integer.class);
        } catch (Exception exception) {
            logger.error("Error in read diagnotic test: ", exception);
            throw new DiagnosticException("Error in read diagnotic test, unexpected exception.", exception);
        }
        logger.debug("Completed diagnotstic read test with query {} ", query );
    }

}
