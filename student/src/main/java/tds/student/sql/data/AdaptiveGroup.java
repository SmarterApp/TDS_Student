/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
public class AdaptiveGroup
{
  private final List<AdaptiveItem> _items = new ArrayList<AdaptiveItem> ();
  private int                      _page;
  private String                   _groupID;
  private long                     _bankKey;
  private int                      _segmentPosition;
  private String                   _segmentID;
  private int                      _numItemsRequired;

  public AdaptiveGroup () {
  }

  public List<AdaptiveItem> getItems () {
    return _items;
  }

  @JsonProperty ("Page")
  public int getPage () {
    return _page;
  }

  public void setPage (int _page) {
    this._page = _page;
  }

  @JsonProperty ("GroupID")
  public String getGroupID () {
    return _groupID;
  }

  public void setGroupID (String _groupID) {
    this._groupID = _groupID;
  }

  @JsonProperty ("BankKey")
  public long getBankKey () {
    return _bankKey;
  }

  public void setBankKey (long _bankKey) {
    this._bankKey = _bankKey;
  }

  @JsonProperty ("SegmentPosition")
  public int getSegmentPosition () {
    return _segmentPosition;
  }

  public void setSegmentPosition (int _segmentPosition) {
    this._segmentPosition = _segmentPosition;
  }

  @JsonProperty ("SegmentID")
  public String getSegmentID () {
    return _segmentID;
  }

  public void setSegmentID (String _segmentID) {
    this._segmentID = _segmentID;
  }

  @JsonProperty ("NumItemsRequired")
  public int getNumItemsRequired () {
    return _numItemsRequired;
  }

  public void setNumItemsRequired (int _numItemsRequired) {
    this._numItemsRequired = _numItemsRequired;
  }

}
