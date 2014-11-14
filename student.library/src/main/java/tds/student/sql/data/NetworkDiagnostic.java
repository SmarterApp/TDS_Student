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
public class NetworkDiagnostic
{
  private String _testLabel;
  private int    _minDataRateRequired;
  private int    _averageItemSize;
  private int    _responseTime;

  @JsonProperty ("TestLabel")
  public String getTestLabel () {
    return _testLabel;
  }

  public void setTestLabel (String _testLabel) {
    this._testLabel = _testLabel;
  }

  @JsonProperty ("MinDataRateRequired")
  public int getMinDataRateRequired () {
    return _minDataRateRequired;
  }

  public void setMinDataRateRequired (int _minDataRateRequired) {
    this._minDataRateRequired = _minDataRateRequired;
  }

  @JsonProperty ("AverageItemSize")
  public int getAverageItemSize () {
    return _averageItemSize;
  }

  public void setAverageItemSize (int _averageItemSize) {
    this._averageItemSize = _averageItemSize;
  }

  @JsonProperty ("ResponseTime")
  public int getResponseTime () {
    return _responseTime;
  }

  public void setResponseTime (int _responseTime) {
    this._responseTime = _responseTime;
  }

}
