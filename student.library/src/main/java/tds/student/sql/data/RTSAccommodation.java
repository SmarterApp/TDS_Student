/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

/**
 * @author temp_rreddy
 * 
 */
import com.fasterxml.jackson.annotation.JsonProperty;

public class RTSAccommodation
{
  private String _accFamily;
  private String _accCode;

  @JsonProperty ("subject")
  public String getAccFamily () {
    return _accFamily;
  }

  public void setAccFamily (String _accFamily) {
    this._accFamily = _accFamily;
  }

  @JsonProperty ("accCode")
  public String getAccCode () {
    return _accCode;
  }

  public void setAccCode (String _accCode) {
    this._accCode = _accCode;
  }

}
