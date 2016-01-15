/*************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package tds.student.sql.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mpatel
 *
 */
public class Dependency
{
  public String  _contextType;
  public String  _context;
  public String  _ifType;
  public String  _ifValue;
  public String  _thenType;
  public String  _thenValue;
  public boolean _isDefault;

  /**
   * @return the _contextType
   */
  @JsonProperty ("ContextType")
  public String getContextType () {
    return _contextType;
  }

  /**
   * @param _contextType
   *          the _contextType to set
   */
  public void setContextType (String _contextType) {
    this._contextType = _contextType;
  }

  /**
   * @return the _context
   */
  @JsonProperty ("Context")
  public String getContext () {
    return _context;
  }

  /**
   * @param _context
   *          the _context to set
   */
  public void setContext (String _context) {
    this._context = _context;
  }

  /**
   * @return the _ifType
   */
  @JsonProperty ("IfType")
  public String getIfType () {
    return _ifType;
  }

  /**
   * @param _ifType
   *          the _ifType to set
   */
  public void setIfType (String _ifType) {
    this._ifType = _ifType;
  }

  /**
   * @return the _ifValue
   */
  @JsonProperty ("IfValue")
  public String getIfValue () {
    return _ifValue;
  }

  /**
   * @param _ifValue
   *          the _ifValue to set
   */
  public void setIfValue (String _ifValue) {
    this._ifValue = _ifValue;
  }

  /**
   * @return the _thenType
   */
  @JsonProperty ("ThenType")
  public String getThenType () {
    return _thenType;
  }

  /**
   * @param _thenType
   *          the _thenType to set
   */
  public void setThenType (String _thenType) {
    this._thenType = _thenType;
  }

  /**
   * @return the _thenValue
   */
  @JsonProperty ("ThenValue")
  public String getThenValue () {
    return _thenValue;
  }

  /**
   * @param _thenValue
   *          the _thenValue to set
   */
  public void setThenValue (String _thenValue) {
    this._thenValue = _thenValue;
  }

  /**
   * @return the _isDefault
   */
  @JsonProperty ("IsDefault")
  public boolean isDefault () {
    return _isDefault;
  }

  /**
   * @param _isDefault
   *          the _isDefault to set
   */
  public void setIsDefault (boolean _isDefault) {
    this._isDefault = _isDefault;
  }

}