/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import TDS.Shared.Data.ReturnStatus;

/**
 * @author temp_rreddy
 * 
 */
public class OpportunityStatus
{
  private OpportunityStatusType _status;
  private Date                  _dateStarted;
  private String                _comment;
  ReturnStatus _returnStatus = null;
  
  @JsonProperty ("status")
  public OpportunityStatusType getStatus () {
    return _status;
  }

  public void setStatus (OpportunityStatusType _status) {
    this._status = _status;
  }

  @JsonProperty ("dateStarted")
  public Date getDateStarted () {
    return _dateStarted;
  }

  public void setDateStarted (Date _dateStarted) {
    this._dateStarted = _dateStarted;
  }

  @JsonProperty ("comment")
  public String getComment () {
    return _comment;
  }

  public void setComment (String _comment) {
    this._comment = _comment;
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
