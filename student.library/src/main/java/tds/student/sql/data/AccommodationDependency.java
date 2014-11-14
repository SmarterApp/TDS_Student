/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
public class AccommodationDependency
{

  private String  _ifType;
  private String  _ifValue;
  private String  _thenType;
  private String  _thenValue;
  private boolean _isDefault;

  public AccommodationDependency (String ifType, String ifValue, String thenType, String thenValue, boolean isDefault) {
    _ifType = ifType;
    _ifValue = ifValue;
    _thenType = thenType;
    _thenValue = thenValue;
    _isDefault = isDefault;
  }

  @JsonProperty ("ifType")
  public String getIfType () {
    return _ifType;
  }

  public void setIfType (String _ifType) {
    this._ifType = _ifType;
  }

  @JsonProperty ("ifValue")
  public String getIfValue () {
    return _ifValue;
  }

  public void setIfValue (String _ifValue) {
    this._ifValue = _ifValue;
  }

  @JsonProperty ("thenType")
  public String getThenType () {
    return _thenType;
  }

  public void setThenType (String _thenType) {
    this._thenType = _thenType;
  }

  @JsonProperty ("thenValue")
  public String getThenValue () {
    return _thenValue;
  }

  public void setThenValue (String _thenValue) {
    this._thenValue = _thenValue;
  }

  @JsonProperty ("isDefault")
  public boolean isDefault () {
    return _isDefault;
  }

  public void setIsDefault (boolean _isDefault) {
    this._isDefault = _isDefault;
  }
}
