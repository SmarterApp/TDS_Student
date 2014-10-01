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
public class ItemStringSet
{
  private String _testKey;
  private int    _opportunity;
  private int    _version;
  private String _itemString;

  public ItemStringSet (String testKey, int opportunity, int version, String itemString) {
    _testKey = testKey;
    _opportunity = opportunity;
    _version = version;
    _itemString = itemString;
  }

  @JsonProperty ("TestKey")
  public String getTestKey () {
    return _testKey;
  }

  public void setTestKey (String _testKey) {
    this._testKey = _testKey;
  }

  @JsonProperty ("Opportunity")
  public int getOpportunity () {
    return _opportunity;
  }

  public void setOpportunity (int _opportunity) {
    this._opportunity = _opportunity;
  }

  @JsonProperty ("Version")
  public int getVersion () {
    return _version;
  }

  public void setVersion (int _version) {
    this._version = _version;
  }

  @JsonProperty ("ItemString")
  public String getItemString () {
    return _itemString;
  }

  public void setItemString (String _itemString) {
    this._itemString = _itemString;
  }

}
