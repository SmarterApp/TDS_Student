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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.student.performance.dao.TestAbilityDao;
import tds.student.performance.dao.mappers.TestAbilityMapper;
import tds.student.performance.domain.TestAbility;
import tds.student.performance.utils.LegacyDbNameUtility;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.*;

/**
 * Created by emunoz on 12/28/15.
 */
@Repository
public class TestAbilityDaoImpl implements TestAbilityDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private LegacyDbNameUtility dbNameUtility;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<TestAbility> getTestAbilities(UUID oppKey, String clientname, String subject, Long testee) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppkey", UuidAdapter.getBytesFromUUID(oppKey));
        parameters.put("clientname", clientname);
        parameters.put("subject", subject);
        parameters.put("testee", testee);

        final String SQL =
                "SELECT\n" +
                    "OTHEROPP._Key as oppkey,\n" +
                    "OTHEROPP._efk_TestID as test,\n" +
                    "OTHEROPP.Opportunity as opportunity,\n" +
                    "OTHEROPP.dateScored as dateScored,\n" +
                    "SCORE.value as score\n" +
                "FROM\n" +
                    "${sessiondb}.testopportunity OTHEROPP, ${sessiondb}.testopportunityscores SCORE\n" +
                "WHERE\n" +
                    "clientname = :clientname AND\n" +
                    "OTHEROPP._efk_Testee = :testee AND\n" +
                    "OTHEROPP.subject = :subject AND\n" +
                    "OTHEROPP.dateDeleted is null AND\n" +
                    "OTHEROPP.dateScored is not null AND\n" +
                    "OTHEROPP._Key <> :oppkey AND\n" +
                    "OTHEROPP._Key = SCORE._fk_TestOpportunity AND\n" +
                    "SCORE.UseForAbility = 1 AND\n" +
                    "SCORE.value is not null";

        return namedParameterJdbcTemplate.query(dbNameUtility.setDatabaseNames(SQL), parameters, new TestAbilityMapper());
    }

    @Override
    public Float getMostRecentTestAbilityFromHistory(String clientname, String subject, Long testee) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientname", clientname);
        parameters.put("subject", subject);
        parameters.put("testee", testee);

        final String SQL =
                "SELECT\n" +
                    "MAX(initialAbility) as ability\n" +
                "FROM\n" +
                    "${sessiondb}.testeehistory\n" +
                "WHERE\n" +
                    "clientname = :clientname AND\n" +
                    "_efk_Testee = :testee AND\n" +
                    "Subject = :subject AND\n" +
                    "initialAbility is not null;";

        return namedParameterJdbcTemplate.queryForObject(dbNameUtility.setDatabaseNames(SQL), parameters, Float.class);
    }
}
