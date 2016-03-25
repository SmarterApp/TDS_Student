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
package tds.student.performance.services.impl;

import AIR.Common.DB.SQLConnection;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.dll.api.ICommonDLL;
import tds.dll.common.performance.utils.LegacySqlConnection;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.services.LegacyTestOpportunityService;

import java.sql.SQLException;

@Service
public class LegacyTestOpportunityServiceImpl implements LegacyTestOpportunityService {
    @Autowired
    ICommonDLL commonDll;

    @Autowired
    LegacySqlConnection legacySqlConnection;

    /**
     * <p>
     *     Opted to let legacy code handle transaction on its own.
     * </p>
     *
     * @param opportunity
     * @param status
     * @throws SQLException
     * @throws ReturnStatusException
     */
    public void setOpportunityStatus(TestOpportunity opportunity, String status) throws SQLException, ReturnStatusException {
        try (SQLConnection connection = legacySqlConnection.get()) {
            commonDll.SetOpportunityStatus_SP(connection, opportunity.getKey(), status, true, opportunity.getSessionKey().toString());
        }
    }
}
