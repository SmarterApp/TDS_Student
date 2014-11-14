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
public class OpportunityAccommodation
{
  private int    _segmentPosition;
  private String _accType;
  private String _accCode;

  @JsonProperty ("SegmentPosition")
  public int getSegmentPosition () {
    return _segmentPosition;
  }

  public void setSegmentPosition (int _segmentPosition) {
    this._segmentPosition = _segmentPosition;
  }

  @JsonProperty ("AccType")
  public String getAccType () {
    return _accType;
  }

  public void setAccType (String _accType) {
    this._accType = _accType;
  }

  @JsonProperty ("AccCode")
  public String getAccCode () {
    return _accCode;
  }

  public void setAccCode (String _accCode) {
    this._accCode = _accCode;
  }

}
