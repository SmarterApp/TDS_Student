/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

public class OpportunityStatusChange
{
  private OpportunityStatusType _status;
  private boolean               _checkReturnStatus;
  private String                _reason;

  public OpportunityStatusType getStatus () {
    return _status;
  }

  public void setStatus (OpportunityStatusType status) {
    _status = status;
  }

  public boolean isCheckReturnStatus () {
    return _checkReturnStatus;
  }

  public void setCheckReturnStatus (boolean checkReturnStatus) {
    _checkReturnStatus = checkReturnStatus;
  }

  public String getReason () {
    return _reason;
  }

  public void setReason (String reason) {
    _reason = reason;
  }

  public OpportunityStatusChange (OpportunityStatusType status, boolean checkReturnStatus, String reason) {
    _status = status;
    _checkReturnStatus = checkReturnStatus;
    _reason = reason;
  }

  public OpportunityStatusChange (OpportunityStatusType status, boolean checkReturnStatus) {
    _status = status;
    _checkReturnStatus = checkReturnStatus;
  }

  public OpportunityStatusChange (OpportunityStatusType status) {
    _status = status;
  }
}
