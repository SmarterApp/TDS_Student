/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.abstractions;

import java.util.UUID;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.student.services.data.TestScoreStatus;

/**
 * @author temp_rreddy
 * 
 */
public interface ITestScoringService
{
  boolean canCompleteTest (UUID oppKey, String testKey) throws ReturnStatusException;

  TestScoreStatus scoreTest (UUID oppKey, String testKey) throws ReturnStatusException;
}
