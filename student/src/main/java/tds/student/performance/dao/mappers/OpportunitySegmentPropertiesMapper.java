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
import tds.dll.common.performance.utils.UuidAdapter;
import tds.student.performance.domain.OpportunitySegmentProperties;
import tds.student.performance.domain.TestSession;

import java.sql.ResultSet;
import java.sql.SQLException;



public class OpportunitySegmentPropertiesMapper implements RowMapper<OpportunitySegmentProperties> {

    @Override
    public OpportunitySegmentProperties mapRow(ResultSet resultSet, int i) throws SQLException {
        OpportunitySegmentProperties segmentProperties = new OpportunitySegmentProperties();

        segmentProperties.set_fk_TestOpportunity(UuidAdapter.getUUIDFromBytes(resultSet.getBytes("_fk_TestOpportunity")));
        segmentProperties.set_efk_Segment(resultSet.getString("_efk_Segment"));
        segmentProperties.setSegmentId(resultSet.getString("segmentId"));
        segmentProperties.setSegmentPosition(resultSet.getInt("segmentPosition"));
        segmentProperties.setAlgorithm(resultSet.getString("algorithm"));
        segmentProperties.setOpItemCnt(resultSet.getInt("opItemCnt"));
        segmentProperties.setIsPermeable(resultSet.getInt("isPermeable"));
        segmentProperties.setSatisfied(resultSet.getBoolean("isSatisfied"));
        segmentProperties.set_date(resultSet.getTimestamp("_date"));

/*
         private UUID _fk_TestOpportunity;
         private String _efk_Segment;
         private String segmentID;
         private Integer segmentPosition;
         private String algorithm;
        private Integer opItemCnt;
        private Integer isPermeable;
        private Boolean isSatisfied;
        private Timestamp _date;
*/

        return segmentProperties;
    }
}
