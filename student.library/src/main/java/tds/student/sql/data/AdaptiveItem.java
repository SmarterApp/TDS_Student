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
public class AdaptiveItem
{
  private String  _itemID;
  private String  _groupID;
  private boolean _isRequired;
  private boolean _isFieldTest;
  private int     _page;
  private int     _position;
  private int     _segment;
  private String  _segmentID;

  @JsonProperty ("ItemID")
  public String getItemID () {
    return _itemID;
  }

  public void setItemID (String _itemID) {
    this._itemID = _itemID;
  }

  @JsonProperty ("GroupID")
  public String getGroupID () {
    return _groupID;
  }

  public void setGroupID (String _groupID) {
    this._groupID = _groupID;
  }

  @JsonProperty ("IsRequired")
  public boolean isRequired () {
    return _isRequired;
  }

  public void setIsRequired (boolean _isRequired) {
    this._isRequired = _isRequired;
  }

  @JsonProperty ("IsFieldTest")
  public boolean isFieldTest () {
    return _isFieldTest;
  }

  public void setIsFieldTest (boolean _isFieldTest) {
    this._isFieldTest = _isFieldTest;
  }

  @JsonProperty ("Page")
  public int getPage () {
    return _page;
  }

  public void setPage (int _page) {
    this._page = _page;
  }

  @JsonProperty ("Position")
  public int getPosition () {
    return _position;
  }

  public void setPosition (int _position) {
    this._position = _position;
  }

  @JsonProperty ("Segment")
  public int getSegment () {
    return _segment;
  }

  public void setSegment (int _segment) {
    this._segment = _segment;
  }

  @JsonProperty ("SegmentID")
  public String getSegmentID () {
    return _segmentID;
  }

  public void setSegmentID (String _segmentID) {
    this._segmentID = _segmentID;
  }

}
