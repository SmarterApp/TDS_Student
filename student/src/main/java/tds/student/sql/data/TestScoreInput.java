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
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import TDS.Shared.Data.ReturnStatus;

public class TestScoreInput
{

  private String _itemString;
  private Date   _dateCompleted;
  private ReturnStatus _returnStatus = null;
  
  @JsonProperty ("key")
  public String getItemString () {
    return _itemString;
  }

  public void setItemString (String _itemString) {
    this._itemString = _itemString;
  }

  @JsonProperty ("DateCompleted")
  public Date getDateCompleted () {
    return _dateCompleted;
  }

  public void setDateCompleted (Date _dateCompleted) {
    this._dateCompleted = _dateCompleted;
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
