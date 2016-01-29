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
package tds.student.performance.utils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.TestSession;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for exploratory testing of various data access methods.
 */
public class DataAccessTest extends IntegrationTest {
    @Autowired
    LegacyDbNameUtility dbNameUtility;

    /**
     * The intent of this test is to determine if the {@code BeanRowPropertyMapper} sets primitive types
     * to 0 or null.  That is, will the {@code BeanRowPropertyMapper} set an {@code Integer} to 0 when the
     * value is null in the database?
     */
    @Test
    public void should_Have_a_Null_Proctor_Id_for_Guest_TestSession() {
        String sessionId = "GUEST Session";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sessionId", sessionId);
        final String SQL =
                "SELECT\n" +
                "   _efk_proctor AS proctorId,\n" +
                "   sessionid AS sessionId,\n" +
                "   proctorname AS proctorName\n" +
                "FROM\n" +
                "   ${sessiondb}.session\n" +
                "WHERE\n" +
                "   sessionid = :sessionId";

        TestSession result = namedParameterJdbcTemplate.queryForObject(
                dbNameUtility.setDatabaseNames(SQL),
                parameters,
                new BeanPropertyRowMapper<>(TestSession.class));

        Assert.assertNotNull(result);
        Assert.assertNull(result.getProctorId());
        Assert.assertNull(result.getProctorName());
        Assert.assertNotNull(result.getSessionId());
    }
}
