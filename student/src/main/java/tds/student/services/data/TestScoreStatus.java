/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.data;

/**
 * @author temp_rreddy
 * 
 */
public enum TestScoreStatus {
  // / <summary>
  // / The test has been submitted to QA.
  // / </summary>
  Submitted,

  // / <summary>
  // / The test is still waiting on some items to be scored.
  // / </summary>
  Waiting,

  // / <summary>
  // / There was an unrecoverable error scoring the test.
  // / </summary>
  Error
}
