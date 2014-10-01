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
 *         Class formerly called "LoginRequirement. Renamed to
 *         TesteeAttributeMetadata because it can hold information about
 *         attributes that are not required at login.
 */
public class TesteeAttributeMetadata
{
  private String  _rtsName;
  private String  _id;
  private String  _label;
  private String  _type;
  private String  _atLogin;
  private int     _sortOrder;
  private boolean _latencySite;

  @JsonProperty ("id")
  public String getId () {
    return _id;
  }

  public void setId (String _id) {
    this._id = _id;
  }

  @JsonProperty ("label")
  public String getLabel () {
    return _label;
  }

  public void setLabel (String _label) {
    this._label = _label;
  }

  @JsonProperty ("sortOrder")
  public int getSortOrder () {
    return _sortOrder;
  }

  public void setSortOrder (int _sortOrder) {
    this._sortOrder = _sortOrder;
  }

  public String getRtsName () {
    return _rtsName;
  }

  public void setRtsName (String value) {
    this._rtsName = value;
  }

  public String getType () {
    return _type;
  }

  public void setType (String value) {
    _type = value;
  }

  public String getAtLogin () {
    return _atLogin;
  }

  public void setAtLogin (String value) {
    _atLogin = value;
  }

  public boolean isLatencySite () {
    return _latencySite;
  }

  public void setLatencySite (boolean value) {
    _latencySite = value;
  }

}
