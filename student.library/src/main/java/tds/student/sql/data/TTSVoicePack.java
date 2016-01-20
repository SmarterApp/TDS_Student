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
public class TTSVoicePack
{
  private String _os;
  private String _name;
  private int    _priority;
  private String _languageCode;

  @JsonProperty ("OS")
  public String getOs () {
    return _os;
  }

  public void setOs (String _os) {
    this._os = _os;
  }

  @JsonProperty ("Name")
  public String getName () {
    return _name;
  }

  public void setName (String _name) {
    this._name = _name;
  }

  @JsonProperty ("Priority")
  public int getPriority () {
    return _priority;
  }

  public void setPriority (int _priority) {
    this._priority = _priority;
  }

  @JsonProperty ("LanguageCode")
  public String getLanguageCode () {
    return _languageCode;
  }

  public void setLanguageCode (String _languageCode) {
    this._languageCode = _languageCode;
  }

}
