/*************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2015 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package tds.student.sql.abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author jmambo
 *
 */
public interface ITestRepository
{

 /**
  * 
  * @param testeeId
  * @param testKey
  * @return
  * @throws ReturnStatusException
  */
  String getTrTestId (String testeeId, String testKey) throws ReturnStatusException;


}
