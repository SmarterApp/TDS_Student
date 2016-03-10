/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 * <p/>
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 * <p/>
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
package tds.student.diagnostic.dao;

import tds.student.diagnostic.exceptions.DiagnosticException;

public interface WriteTestDao {

    void writeSessionDatabase() throws DiagnosticException;

    void writeItemBankDatabase() throws DiagnosticException;

    void writeConfigsDatabase() throws DiagnosticException;

    void writeArchiveDatabase() throws DiagnosticException;

}
