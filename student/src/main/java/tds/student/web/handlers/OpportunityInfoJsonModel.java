/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import java.util.List;

import tds.student.sql.data.TestForm;

/**
 * @author temp_rreddy
 * 
 */
public class OpportunityInfoJsonModel
{
  List<String>   _testeeForms = null;
  List<TestForm> _testForms   = null;

  /**
   * @return the testeeForms
   */
  public List<String> getTesteeForms () {
    return _testeeForms;
  }

  /**
   * @param testeeForms
   *          the testeeForms to set
   */
  public void setTesteeForms (List<String> testeeForms) {
    this._testeeForms = testeeForms;
  }

  /**
   * @return the testForms
   */
  public List<TestForm> getTestForms () {
    return _testForms;
  }

  /**
   * @param testForms
   *          the testForms to set
   */
  public void setTestForms (List<TestForm> testForms) {
    this._testForms = testForms;
  }
}
