/*************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *************************************************************************/

package tds.student.sql.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mpatel
 *
 */
public class Data
{
  private int     _segmentPosition;
  private String  _type;
  private String  _code;
  private String  _value;
  private boolean _isVisible;
  private boolean _isSelectable;
  private boolean _allowChange;
  private boolean _studentControl;
  private boolean _isDefault;
  private boolean _allowCombine;
  private boolean _isFunctional;
  private String  _dependsOnToolType;
  private boolean _disableOnGuestSession;
  private int     _toolTypeSortOrder;
  private int     _toolValueSortOrder;

  /**
   * @return the _segmentPosition
   */
  @JsonProperty ("SegmentPosition")
  public int getSegmentPosition () {
    return _segmentPosition;
  }

  /**
   * @param _segmentPosition
   *          the _segmentPosition to set
   */
  public void setSegmentPosition (int _segmentPosition) {
    this._segmentPosition = _segmentPosition;
  }

  /**
   * @return the _type
   */
  @JsonProperty ("Type")
  public String getType () {
    return _type;
  }

  /**
   * @param _type
   *          the _type to set
   */
  public void setType (String _type) {
    this._type = _type;
  }

  /**
   * @return the _code
   */
  @JsonProperty ("Code")
  public String getCode () {
    return _code;
  }

  /**
   * @param _code
   *          the _code to set
   */
  public void setCode (String _code) {
    this._code = _code;
  }

  /**
   * @return the _value
   */
  @JsonProperty ("Value")
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
   * @return the _isVisible
   */
  @JsonProperty ("IsVisible")
  public boolean isVisible () {
    return _isVisible;
  }

  /**
   * @param _isVisible
   *          the _isVisible to set
   */
  public void setIsVisible (boolean _isVisible) {
    this._isVisible = _isVisible;
  }

  /**
   * @return the _isSelectable
   */
  @JsonProperty ("IsSelectable")
  public boolean isSelectable () {
    return _isSelectable;
  }

  /**
   * @param _isSelectable
   *          the _isSelectable to set
   */
  public void setIsSelectable (boolean _isSelectable) {
    this._isSelectable = _isSelectable;
  }

  /**
   * @return the _allowChange
   */
  @JsonProperty ("AllowChange")
  public boolean isAllowChange () {
    return _allowChange;
  }

  /**
   * @param _allowChange
   *          the _allowChange to set
   */
  public void setAllowChange (boolean _allowChange) {
    this._allowChange = _allowChange;
  }

  /**
   * @return the _studentControl
   */
  @JsonProperty ("StudentControl")
  public boolean isStudentControl () {
    return _studentControl;
  }

  /**
   * @param _studentControl
   *          the _studentControl to set
   */
  public void setStudentControl (boolean _studentControl) {
    this._studentControl = _studentControl;
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

  /**
   * @return the _allowCombine
   */
  @JsonProperty ("AllowCombine")
  public boolean isAllowCombine () {
    return _allowCombine;
  }

  /**
   * @param _allowCombine
   *          the _allowCombine to set
   */
  public void setAllowCombine (boolean _allowCombine) {
    this._allowCombine = _allowCombine;
  }

  /**
   * @return the _isFunctional
   */
  @JsonProperty ("IsFunctional")
  public boolean isFunctional () {
    return _isFunctional;
  }

  /**
   * @param _isFunctional
   *          the _isFunctional to set
   */
  public void setIsFunctional (boolean _isFunctional) {
    this._isFunctional = _isFunctional;
  }

  /**
   * @return the _dependsOnToolType
   */
  @JsonProperty ("DependsOnToolType")
  public String getDependsOnToolType () {
    return _dependsOnToolType;
  }

  /**
   * @param _dependsOnToolType
   *          the _dependsOnToolType to set
   */
  public void setDependsOnToolType (String _dependsOnToolType) {
    this._dependsOnToolType = _dependsOnToolType;
  }

  /**
   * @return the _disableOnGuestSession
   */
  @JsonProperty ("DisableOnGuestSession")
  public boolean isDisableOnGuestSession () {
    return _disableOnGuestSession;
  }

  /**
   * @param _disableOnGuestSession
   *          the _disableOnGuestSession to set
   */
  public void setDisableOnGuestSession (boolean _disableOnGuestSession) {
    this._disableOnGuestSession = _disableOnGuestSession;
  }

  /**
   * @return the _toolTypeSortOrder
   */
  @JsonProperty ("ToolTypeSortOrder")
  public int getToolTypeSortOrder () {
    return _toolTypeSortOrder;
  }

  /**
   * @param _toolTypeSortOrder
   *          the _toolTypeSortOrder to set
   */
  public void setToolTypeSortOrder (int _toolTypeSortOrder) {
    this._toolTypeSortOrder = _toolTypeSortOrder;
  }

  /**
   * @return the _toolValueSortOrder
   */
  @JsonProperty ("ToolValueSortOrder")
  public int getToolValueSortOrder () {
    return _toolValueSortOrder;
  }

  /**
   * @param _toolValueSortOrder
   *          the _toolValueSortOrder to set
   */
  public void setToolValueSortOrder (int _toolValueSortOrder) {
    this._toolValueSortOrder = _toolValueSortOrder;
  }

}