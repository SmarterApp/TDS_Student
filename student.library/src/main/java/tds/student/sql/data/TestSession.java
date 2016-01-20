/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

/**
 * @author temp_rreddy
 * 
 */
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import TDS.Shared.Data.ReturnStatus;

import tds.student.sql.data.SessionStatus.SessionStatusType;

public class TestSession
{
  private SessionStatusType _statusType;
  private String            _id;
  private UUID              _key;
  private String            _name;
  private Date              _dateCreated;
  private Date              _dateBegin;
  private Date              _dateEnd;
  private UUID              _browserKey;
  private boolean           _isProctorless;
  private int               _needApproval;
  private ReturnStatus      _returnStatus = null;
  private boolean           _isGuest;

  @JsonProperty ("status")
  public SessionStatusType getStatusType () {
    return _statusType;
  }

  public void setStatusType (SessionStatusType _statusType) {
    this._statusType = _statusType;
  }

  @JsonProperty ("id")
  public String getId () {
    return _id;
  }

  public void setId (String _id) {
    this._id = _id;
  }

  @JsonProperty ("key")
  public UUID getKey () {
    return _key;
  }

  public void setKey (UUID _key) {
    this._key = _key;
  }

  @JsonProperty ("name")
  public String getName () {
    return _name;
  }

  public void setName (String _name) {
    this._name = _name;
  }

  @JsonProperty ("dateCreated")
  public Date getDateCreated () {
    return _dateCreated;
  }

  public void setDateCreated (Date _dateCreated) {
    this._dateCreated = _dateCreated;
  }

  @JsonProperty ("dateBegin")
  public Date getDateBegin () {
    return _dateBegin;
  }

  public void setDateBegin (Date _dateBegin) {
    this._dateBegin = _dateBegin;
  }

  @JsonProperty ("dateEnd")
  public Date getDateEnd () {
    return _dateEnd;
  }

  public void setDateEnd (Date _dateEnd) {
    this._dateEnd = _dateEnd;
  }

  @JsonProperty ("browserKey")
  public UUID getBrowserKey () {
    return _browserKey;
  }

  public void setBrowserKey (UUID _browserKey) {
    this._browserKey = _browserKey;
  }

  @JsonProperty ("isProctorless")
  public boolean isProctorless () {
    // TODO
    return (!StringUtils.isEmpty (this.getId ()) && "GUEST Session".equalsIgnoreCase (this.getId ()));
  }

  public void setIsProctorless (boolean _isProctorless) {
    this._isProctorless = _isProctorless;
  }

  @JsonProperty ("NeedApproval")
  public int getNeedApproval () {
    return _needApproval;
  }

  public void setNeedApproval (int _needApproval) {
    this._needApproval = _needApproval;
  }

  /**
   * @return the _returnStatus
   */
  public ReturnStatus getReturnStatus () {
    return _returnStatus;
  }

  /**
   * @param _returnStatus
   *          the _returnStatus to set
   */
  public void setReturnStatus (ReturnStatus _returnStatus) {
    this._returnStatus = _returnStatus;
  }

  @JsonProperty ("isGuest")
  public boolean getIsGuest () {
    return (!StringUtils.isEmpty (this._id) && "GUEST Session".equals (this._id));
  }

  public void setIsGuest (boolean value) {
    this._isGuest = value;
  }

}
