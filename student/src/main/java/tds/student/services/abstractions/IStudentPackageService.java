/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.abstractions;

import org.opentestsystem.shared.trapi.data.TestStatus;

import TDS.Shared.Exceptions.ReturnStatusException;

public interface IStudentPackageService
{
  /**
   * Gets Student Package XML
   * 
   * @param ssidOrExternalSsid
   * @param stateCode
   * @param isSsid
   * @return Student Package XML
   * @throws ReturnStatusException 
   */
  String getStudentPackage(String ssidOrExternalSsid, boolean isSsid);

  /**
   * Send testStatus
   * 
   * @param testStatus
   */
  void sendTestStatus (TestStatus testStatus);

}
