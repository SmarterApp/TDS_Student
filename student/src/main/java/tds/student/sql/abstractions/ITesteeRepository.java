/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.abstractions;

import java.util.List;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.student.sql.data.RTSAccommodation;

import tds.student.sql.data.TesteeAttributes;

/**
 * 
 * @author temp_rreddy
 * 
 * 
 */

public interface ITesteeRepository

{

  // TDSSqlResult<Testee> Login(string externalID, string sessionID);

  TesteeAttributes login (String keyValues, String sessionID) throws ReturnStatusException;

  List<RTSAccommodation> getAccommodations (long testeeKey) throws ReturnStatusException;

}
