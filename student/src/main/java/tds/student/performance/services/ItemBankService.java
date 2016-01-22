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

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.sql.data.AccList;
import tds.student.sql.data.TestForm;
import tds.student.sql.data.TestProperties;

import java.util.List;

public interface ItemBankService {
    List<String> listTests() throws ReturnStatusException;

    TestProperties getTestProperties(String testKey) throws ReturnStatusException;

    AccList getTestAccommodations(String testKey) throws ReturnStatusException;

    List<String> getGrades() throws ReturnStatusException;

    List<TestForm> getTestForms(String testID) throws ReturnStatusException;

    String getItemPath(long bankKey, long itemKey) throws ReturnStatusException;

    String getStimulusPath(long bankKey, long stimulusKey) throws ReturnStatusException;
}
