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

import TDS.Shared.Data.ReturnStatus;

/**
 * @author temp_rreddy
 * 
 */
public class TesteeAttributes extends RTSAttributes
{
  private long _key;
  private ReturnStatus _returnStatus = null;
  
  @JsonProperty ("Key")
  public long getKey () {
    return _key;
  }

  public void setKey (long _key) {
    this._key = _key;
  }

  /**
   * @return the _returnStatus
   */
  public ReturnStatus getReturnStatus () {
    return _returnStatus;
  }

  /**
   * @param _returnStatus the _returnStatus to set
   */
  public void setReturnStatus (ReturnStatus _returnStatus) {
    this._returnStatus = _returnStatus;
  }

  
}
