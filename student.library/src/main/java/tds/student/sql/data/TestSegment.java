/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
public class TestSegment
{
  private String  _id;
  private int     _position;
  private String  _label;
  private int     _isPermeable;
  private int     _entryApproval;
  private int     _exitApproval;
  private boolean _itemReview;
  private int     _updatePermable;

  public TestSegment () {
  }

  public TestSegment (String id, int position, String label, int isPermeable, int entryApproval, int exitApproval, boolean itemReview) {
    _id = id;
    _position = position;
    _label = label;
    _isPermeable = isPermeable;
    _entryApproval = entryApproval;
    _exitApproval = exitApproval;
    _itemReview = itemReview;
  }

  @JsonProperty ("ID")
  public String getId () {
    return _id;
  }

  public void setId (String _id) {
    this._id = _id;
  }

  @JsonProperty ("Position")
  public int getPosition () {
    return _position;
  }

  public void setPosition (int _position) {
    this._position = _position;
  }

  @JsonProperty ("Label")
  public String getLabel () {
    return _label;
  }

  public void setLabel (String _label) {
    this._label = _label;
  }

  @JsonProperty ("IsPermeable")
  public int getIsPermeable () {
    return _isPermeable;
  }

  public void setIsPermeable (int _isPermeable) {
    this._isPermeable = _isPermeable;
  }

  @JsonProperty ("EntryApproval")
  public int getEntryApproval () {
    return _entryApproval;
  }

  public void setEntryApproval (int _entryApproval) {
    this._entryApproval = _entryApproval;
  }

  @JsonProperty ("ExitApproval")
  public int getExitApproval () {
    return _exitApproval;
  }

  public void setExitApproval (int _exitApproval) {
    this._exitApproval = _exitApproval;
  }

  @JsonProperty ("ItemReview")
  public boolean isItemReview () {
    return _itemReview;
  }

  public void setItemReview (boolean _itemReview) {
    this._itemReview = _itemReview;
  }

  @JsonProperty ("updatePermeable")
  public int getUpdatePermable () {
    return _updatePermable;
  }

  public void setUpdatePermable (int value) {
    this._updatePermable = value;
  }

  public enum TestSegmentApproval {
    Entry, Exit
  }

}
