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

import tds.student.performance.domain.*;

import java.util.List;

/**
 * Created by jjohnson on 12/25/15.
 */
public interface ConfigurationDao {
    List<ClientSystemFlag> getSystemFlags(String clientName);
    ClientTestProperty getClientTestProperty(String clientName, String testId);
    List<StudentLoginField> getStudentLoginFields(String clientName);
    ConfigTestToolType getTestToolType(String clientName, String toolName, String contextType, String context);
    Boolean isSetForScoreByTDS(String clientName, String testId);
    Externs getExterns(String clientName);
}
