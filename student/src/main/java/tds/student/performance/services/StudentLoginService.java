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

import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.MultiDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.Map;

public interface StudentLoginService {

    MultiDataResultSet login(SQLConnection connection, String clientname, Map<String, String> keyValues, String sessionId)
            throws ReturnStatusException;

}
