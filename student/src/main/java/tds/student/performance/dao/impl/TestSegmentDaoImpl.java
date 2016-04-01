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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Repository;
import tds.dll.common.performance.caching.CacheType;
import tds.dll.common.performance.utils.LegacyDbNameUtility;
import tds.dll.common.performance.utils.UuidAdapter;
import tds.student.performance.dao.TestSegmentDao;
import tds.student.performance.dao.mappers.TestAbilityMapper;
import tds.student.performance.domain.InsertTesteeResponse;
import tds.student.performance.domain.ItemForTesteeResponse;
import tds.student.performance.domain.TestAbility;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class TestSegmentDaoImpl implements TestSegmentDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private LegacyDbNameUtility dbNameUtility;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Gets the test length by querying the testopportunitysegment table.
     *
     * @param oppKey the opportunity key of the segment length to obtain.
     * @return
     */
    @Override
    public Integer getTestLengthForOpportunitySegment(UUID oppKey) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));

        final String SQL =
            "SELECT\n" +
                "CONVERT(SUM(opItemCnt), SIGNED) + CONVERT(SUM(ftItemCnt), SIGNED) AS testLength\n" +
            "FROM\n" +
                "${sessiondb}.testopportunitysegment\n" +
            "WHERE\n" +
                "_fk_TestOpportunity = :oppKey";

        return namedParameterJdbcTemplate.queryForInt(dbNameUtility.setDatabaseNames(SQL), parameters);
    }
}
