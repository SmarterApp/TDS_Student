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
public class TestLanguage
{
  private String  _key;
  private String  _language;
  private boolean _isSelectable;
  private String  _selectionAlgorithm;
  private String  _formSelection;

  @JsonProperty ("Text")
  public String getKey () {
    return _key;
  }

  public void setKey (String _key) {
    this._key = _key;
  }

  @JsonProperty ("Language")
  public String getLanguage () {
    return _language;
  }

  public void setLanguage (String _language) {
    this._language = _language;
  }

  @JsonProperty ("IsSelectable")
  public boolean isSelectable () {
    return _isSelectable;
  }

  public void setIsSelectable (boolean _isSelectable) {
    this._isSelectable = _isSelectable;
  }

  @JsonProperty ("SelectionAlgorithm")
  public String getSelectionAlgorithm () {
    return _selectionAlgorithm;
  }

  public void setSelectionAlgorithm (String _selectionAlgorithm) {
    this._selectionAlgorithm = _selectionAlgorithm;
  }

  @JsonProperty ("FormSelection")
  public String getFormSelection () {
    return _formSelection;
  }

  public void setFormSelection (String _formSelection) {
    this._formSelection = _formSelection;
  }

  public TestLanguage () {
  }

  public TestLanguage (String key, String language) {
    _key = key;
    _language = language;
    _isSelectable = true;
  }
}
