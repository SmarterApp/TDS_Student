/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.data;

import java.util.List;

import tds.student.sql.data.Accommodations;
import tds.student.sql.data.OpportunityStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 *
 */
public class ApprovalInfo
{
  
  private OpportunityApprovalStatus _status;
  private String _comment;
  private List<Accommodations> _segmentsAccommodations;
  
  
  /**
   * @return the _status
   */
  @JsonProperty ("status")
  public int getNumericStatus()
  {
    return getStatus ().ordinal ();
  }
    
  @JsonIgnore
  public OpportunityApprovalStatus getStatus () {
    return _status;
  }

  /**
   * @param _status the _status to set
   */
  public void setStatus (OpportunityApprovalStatus _status) {
    this._status = _status;
  }

  /**
   * @return the _comment
   */
  @JsonProperty ("comment")
  public String getComment () {
    return _comment;
  }

  /**
   * @param _comment the _comment to set
   */
  public void setComment (String _comment) {
    this._comment = _comment;
  }

  /**
   * @return the _segmentsAccommodations
   */
  @JsonProperty ("segmentsAccommodations")
  public List<Accommodations> getSegmentsAccommodations () {
    return _segmentsAccommodations;
  }

  /**
   * @param _segmentsAccommodations the _segmentsAccommodations to set
   */
  public void setSegmentsAccommodations (List<Accommodations> _segmentsAccommodations) {
    this._segmentsAccommodations = _segmentsAccommodations;
  }

  public ApprovalInfo(OpportunityStatus oppStatus)
  {
      if (oppStatus == null || oppStatus.getStatus() == null) {
          _status = OpportunityApprovalStatus.Logout;
          return;
      }

      // map the opportunity status to what we understand as the approval status
      switch (oppStatus.getStatus ())
      {
          case Approved:
              _status = OpportunityApprovalStatus.Approved;
              break;
          case Denied:
              _status = OpportunityApprovalStatus.Denied;
              break;
          case Paused:
              _status = OpportunityApprovalStatus.Logout;
              break;
          default:
              _status = OpportunityApprovalStatus.Waiting;
              break;
      }
      
      _comment = oppStatus.getComment ();
  }
  
  public enum OpportunityApprovalStatus {
    Waiting(0), Approved(1), Denied(2), Logout(3);

    private int _code;

    OpportunityApprovalStatus (int code) {
      _code = code;
    }

    public int getCode () {
      return _code;
    }
  }
  //TODO
//  public enum OpportunityApprovalStatus
//  {
//      Waiting = 0,
//      Approved = 1,
//      Denied = 2,
//      Logout = 3
//  }
}
