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
package tds.student.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import tds.student.performance.utils.UuidAdapter;
import tds.student.performance.domain.TestSession;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Map a record from the {@code session.session} table to a {@code TestSession} object.
 */
public class TestSessionMapper implements RowMapper<TestSession> {

    @Override
    public TestSession mapRow(ResultSet resultSet, int i) throws SQLException {
        TestSession testSession = new TestSession();
        testSession.setKey(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("key")));
        testSession.setSessionType((Integer)resultSet.getObject("sessionType"));
        testSession.setStatus(resultSet.getString("status"));
        testSession.setDateBegin(resultSet.getTimestamp("dateBegin"));
        testSession.setDateEnd(resultSet.getTimestamp("dateEnd"));
        testSession.setDateVisited(resultSet.getTimestamp("dateVisited"));
        testSession.setClientName(resultSet.getString("clientName"));
        testSession.setProctorId((Long)resultSet.getObject("proctor"));
        testSession.setProctorName(resultSet.getString("proctorName"));
        testSession.setSessionId(resultSet.getString("sessionId"));
        testSession.setSessionBrowser(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("sessionBrowser")));

        return testSession;
    }
}
