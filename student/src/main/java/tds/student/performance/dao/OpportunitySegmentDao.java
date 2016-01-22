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
package tds.student.performance.dao;

import tds.student.performance.domain.InsertTesteeResponse;
import tds.student.performance.domain.ItemForTesteeResponse;
import tds.student.performance.domain.OpportunitySegment;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public interface OpportunitySegmentDao {

    OpportunitySegment getOpportunitySegmentAccommodation(UUID oppKey, Integer segment);

    List<ItemForTesteeResponse> getItemForTesteeResponse(String adminSubject, String testForm, String groupId, String languagePropertyValue);

    Boolean existsTesteeResponsesByBankKeyAndOpportunity(UUID oppKey, List<String> itemKeys);

    String loadInsertTableForTesteeResponses(Connection connection,  List<InsertTesteeResponse> itemList);

    void dropTempTable(Connection connection, String tableName);


    }
