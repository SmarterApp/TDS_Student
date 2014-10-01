/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;

/**
 * @author temp_rreddy
 * 
 */
public class LoginInfo
{
  private List<String> _validationErrors = new ArrayList<String> ();

  private TestSession   _session;
  private Testee        _testee;

  /**
   * @return the _session
   */
  @JsonProperty ("session")
  public TestSession getSession () {
    return _session;
  }

  /**
   * @param _session
   *          the _session to set
   */
  public void setSession (TestSession _session) {
    this._session = _session;
  }

  /**
   * @return the _testee
   */
  @JsonProperty ("testee")
  public Testee getTestee () {
    return _testee;
  }

  /**
   * @param _testee
   *          the _testee to set
   */
  public void setTestee (Testee _testee) {
    this._testee = _testee;
  }

  /**
   * @return the _validationErrors
   */
  @JsonProperty ("errors")
  public List<String> getValidationErrors () {
    return _validationErrors;
  }

  /**
   * @param _validationErrors
   *          the _validationErrors to set
   */
  public void setValidationErrors (List<String> _validationErrors) {
    this._validationErrors = _validationErrors;
  }

  public void addValidationMessage (String error)
  {
    _validationErrors.add (error);
  }

  public void addValidationError (String error)
  {
    _validationErrors.add (error);
  }
}
