/*************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package tds.student.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mskhan
 * 
 */
public class Segment
{
  private String  _id;

  private int     _position;

  private String  _label;

  private boolean _itemReview;

  private int     _isPermable;

  private int     _updatePermable;

  private int     _entryApproval;

  private int     _exitApproval;

  @JsonProperty ("id")
  public String getId () {
    return _id;
  }

  public void setId (String value) {
    this._id = value;
  }

  @JsonProperty ("position")
  public int getPosition () {
    return _position;
  }

  public void setPosition (int value) {
    this._position = value;
  }

  @JsonProperty ("label")
  public String getLabel () {
    return _label;
  }

  public void setLabel (String value) {
    this._label = value;
  }

  @JsonProperty ("itemReview")
  public boolean isItemReview () {
    return _itemReview;
  }

  public void setItemReview (boolean value) {
    this._itemReview = value;
  }

  @JsonProperty ("isPermeable")
  public int getIsPermable () {
    return _isPermable;
  }

  public void setIsPermable (int value) {
    this._isPermable = value;
  }

  @JsonProperty ("updatePermeable")
  public int getUpdatePermable () {
    return _updatePermable;
  }

  public void setUpdatePermable (int value) {
    this._updatePermable = value;
  }

  @JsonProperty ("entryApproval")
  public int getEntryApproval () {
    return _entryApproval;
  }

  public void setEntryApproval (int value) {
    this._entryApproval = value;
  }

  @JsonProperty ("exitApproval")
  public int getExitApproval () {
    return _exitApproval;
  }

  public void setExitApproval (int value) {
    this._exitApproval = value;
  }
}
