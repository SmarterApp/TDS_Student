/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.services.data.LoginInfo;
import tds.student.services.data.LoginKeyValues;
import tds.student.sql.data.Accommodations;

/**
 * @author temp_rreddy
 * 
 */
public interface ILoginService
{
  LoginInfo login (String sessionID, LoginKeyValues keyValues) throws ReturnStatusException;

  // / <summary>
  // / For a collection of accommodations set all the matching RTS defaults
  // / </summary>
  void setRTSAccommodations (long testee, String accFamily, Accommodations accommodations) throws ReturnStatusException;
}
