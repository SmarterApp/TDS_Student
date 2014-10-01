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

import tds.student.sql.data.AccList;

import tds.student.sql.data.TestForm;

import tds.student.sql.data.TestProperties;

/**
 * 
 * @author temp_rreddy
 * 
 * 
 */

public interface IItemBankRepository

{

  List<String> listTests () throws ReturnStatusException;

  // / <summary>
  // / Get the test properties and segments.
  // / </summary>
  TestProperties getTestProperties (String testKey) throws ReturnStatusException;

  // / <summary>
  // / Get the test and segment accommodations.
  // / </summary>
  AccList getTestAccommodations (String testKey) throws ReturnStatusException;

  // / <summary>
  // / Get all the unique grades (used for PT).
  // / </summary>
  List<String> getGrades () throws ReturnStatusException;

  // / <summary>
  // / Get all the test forms (used for data entry).
  // / </summary>
  List<TestForm> getTestForms (String testID) throws ReturnStatusException;

  String getItemPath (long bankKey, long itemKey) throws ReturnStatusException;

  String getStimulusPath (long bankKey, long stimulusKey) throws ReturnStatusException;

}
