/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.annotation.JsonProperty;

import AIR.Common.DB.results.DbResultRecord;

/**
 * @author temp_rreddy
 * 
 */
public class RTSAttribute
{
  private String _id;
  private String _value;
  private String _label;
  private int    _sortOrder;
  private String _atLogin;

  public RTSAttribute () {
  }

  /**
   * @return the _id
   */
  @JsonProperty ("id")
  public String getId () {
    return _id;
  }

  /**
   * @param _id
   *          the _id to set
   */
  public void setId (String _id) {
    this._id = _id;
  }

  /**
   * @return the _value
   */
  @JsonProperty ("value")
  public String getValue () {
    return _value;
  }

  /**
   * @param _value
   *          the _value to set
   */
  public void setValue (String _value) {
    this._value = _value;
  }

  /**
   * @return the _label
   */
  @JsonProperty ("label")
  public String getLabel () {
    return _label;
  }

  /**
   * @param _label
   *          the _label to set
   */
  public void setLabel (String _label) {
    this._label = _label;
  }

  /**
   * @return the _sortOrder
   */
  @JsonProperty ("SortOrder")
  public int getSortOrder () {
    return _sortOrder;
  }

  /**
   * @param _sortOrder
   *          the _sortOrder to set
   */
  public void setSortOrder (int _sortOrder) {
    this._sortOrder = _sortOrder;
  }

  /**
   * @return the _atLogin
   */
  @JsonProperty ("AtLogin")
  public String getAtLogin () {
    return _atLogin;
  }

  /**
   * @param _atLogin
   *          the _atLogin to set
   */
  public void setAtLogin (String _atLogin) {
    this._atLogin = _atLogin;
  }

  public RTSAttribute (String id, String value, String label, int sortOrder, String atLogin) {
    _id = id;
    _value = value;
    _label = label;
    _sortOrder = sortOrder;
    _atLogin = atLogin;
  }

  @Override
  public String toString () {
    return _id;
  }

  public static RTSAttribute parse (DbResultRecord reader) throws SQLException {
    RTSAttribute attribute = new RTSAttribute ();
    attribute._id = reader.<String>get ("TDS_ID");
    attribute._value = reader.<String>get ("value");
    attribute._label = reader.<String>get ("label");
    attribute._sortOrder = reader.<Integer>get ("sortOrder");
    attribute._atLogin = reader.<String>get ("atLogin");

    return attribute;
  }
}
