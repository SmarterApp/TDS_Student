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
package tds.student.performance.services;

import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.performance.domain.ClientTestProperty;
import tds.student.performance.domain.TestOpportunity;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.TestConfig;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by jjohnson on 12/26/15.
 */
public interface TestOpportunityService {
    TestConfig startTestOpportunity(OpportunityInstance opportunityInstance, String testKey, String formKeyList);

    Float getInitialAbility(TestOpportunity opportunity, ClientTestProperty property);
}
