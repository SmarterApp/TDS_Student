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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.dll.common.performance.utils.LegacyDbNameUtility;
import tds.student.performance.dao.ItemBankDao;
import tds.student.sql.data.TestGrade;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for interacting with the {@code itembank} database.
 * <p>
 *     This class could potentially be used for all interaction with the {@code itembank} database.  Normally the DAO
 *     pattern tends to define one DAO object per table.  In this case, it might make more sense to just have one
 *     general DAO that allows us to talk to the {@code itembank} database and get whatever we need.
 * </p>
 */
@Repository
public class ItemBankDaoImpl extends tds.dll.common.performance.dao.impl.ItemBankDaoImpl implements ItemBankDao  {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationDaoImpl.class);

    @Autowired
    private LegacyDbNameUtility dbNameUtility;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<TestGrade> getTestGrades(String clientName, String testKey, Integer sessionType) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put ("testKey", testKey);
        parameters.put ("clientName", clientName);
        parameters.put ("sessionType", sessionType);

        final String SQL = "select distinct grade "
                + " from ${itembankdb}.tblsetofadminsubjects S, ${configdb}.client_testmode M, ${configdb}.client_testgrades G, ${configdb}.client_testwindow W, ${configdb}.client_testproperties P "
                + " where M.clientname = :clientName and (:testKey is null or M.testkey = :testKey) and M.testkey = S._Key and (M.sessionType = -1 or M.sessionType = :sessionType) "
                + "    and M.clientname = G.clientname and M.TestID = G.TestID  and W.clientname = :clientName and W.TestID = M.testID and P.clientname = :clientName and P.TestID = M.testID "
                + "    and P.IsSelectable = 1 and now() between W.startDate and W.endDate order by grade";

        List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(
                dbNameUtility.setDatabaseNames(SQL),
                parameters);

        List<TestGrade> results = new ArrayList<>();

        for (Map<String, Object> row : list) {
            results.add(new TestGrade((String)row.get("grade")));
        }

        return results;
    }
}
