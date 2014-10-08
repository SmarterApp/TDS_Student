/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 *
 */
public class ItemResponseUpdate extends ItemResponseScorable implements IItemResponseUpdate {
  private int _page;
  private String _dateCreated;
  private boolean _isSelected;
  /**
   * Is the testees response to this item valid for this item format.
   */
  private boolean _isValid;
  
  public ItemResponseUpdate()
  {
  }
  public ItemResponseUpdate(String testKey, String testID, String language, int position, int sequence, long bankKey, long itemKey, String segmentID, String value, UUID scoreMark, int page, String dateCreated, boolean isSelected, boolean isValid, String itemFile) 
  {
      super(testKey, testID, language, position, sequence, bankKey, itemKey, segmentID, value, scoreMark, itemFile); 
      _page = page;
      _dateCreated = dateCreated;
      _isSelected = isSelected;
      _isValid = isValid;
  }
  
  /**
   * @return the _isValid
   */
  @JsonProperty("IsValid")
  public boolean getIsValid () {
    return _isValid;
  }
  /**
   * @param _isValid the _isValid to set
   */
  public void setIsValid (boolean _isValid) {
    this._isValid = _isValid;
  }
  @JsonProperty("Page")
  public int getPage () {
    return _page;
  }
  public void setPage (int _page) {
    this._page = _page;
  }
  @JsonProperty("DateCreated")
  public String getDateCreated () {
    return _dateCreated;
  }
  public void setDateCreated (String _dateCreated) {
    this._dateCreated = _dateCreated;
  }
  @JsonProperty("IsSelected")
  public boolean getIsSelected () {
    return _isSelected;
  }
  public void setIsSelected (boolean _isSelected) {
    this._isSelected = _isSelected;
  }
}
