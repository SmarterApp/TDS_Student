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
// / <summary>
// / Data that is returned when starting a test.
// / </summary>
public class TestConfig
{
  private OpportunityStatusType _status;
  private int                   _restart;
  private int                   _testLength;
  private int                   _startPosition;
  private int                   _contentLoadTimeout;
  private int                   _interfaceTimeout;
  private int                   _requestInterfaceTimeout;
  private int                   _oppRestartMins;
  private int                   _prefetch;
  private boolean               _scoreByTDS;
  private boolean               _validateCompleteness;
  private ReturnStatus _returnStatus = null;
  
  /**
   * @return the returnStatus
   */
  public ReturnStatus getReturnStatus () {
    return _returnStatus;
  }

  /**
   * @param returnStatus the returnStatus to set
   */
  public void setReturnStatus (ReturnStatus returnStatus) {
    this._returnStatus = returnStatus;
  }

  // / <summary>
  // / Used for logging the client opportunity.
  // / </summary>
  // / <remarks>
  // / This will not get updated by the cookie. We should
  // / work to remove the need for this.
  // / </remarks>
  @JsonProperty ("Status")
  public OpportunityStatusType getStatus () {
    return _status;
  }

  public void setStatus (OpportunityStatusType _status) {
    this._status = _status;
  }

  @JsonProperty ("Restart")
  public int getRestart () {
    return _restart;
  }

  public void setRestart (int _restart) {
    this._restart = _restart;
  }

  // / <summary>
  // / The # of questions on the test.
  // / </summary>
  @JsonProperty ("testLength")
  public int getTestLength () {
    return _testLength;
  }

  public void setTestLength (int _testLength) {
    this._testLength = _testLength;
  }

  // / <summary>
  // / What question to first show the student.
  // / </summary>
  @JsonProperty ("startPosition")
  public int getStartPosition () {
    return _startPosition;
  }

  public void setStartPosition (int _startPosition) {
    this._startPosition = _startPosition;
  }

  // / <summary>
  // / The # of seconds until you abort trying to load content.
  // / </summary>
  @JsonProperty ("contentLoadTimeout")
  public int getContentLoadTimeout () {
    return _contentLoadTimeout;
  }

  public void setContentLoadTimeout (int _contentLoadTimeout) {
    this._contentLoadTimeout = _contentLoadTimeout;
  }

  // / <summary>
  // / The # of minutes the student can be idle before logging them out.
  // / </summary>
  @JsonProperty ("interfaceTimeout")
  public int getInterfaceTimeout () {
    return _interfaceTimeout;
  }

  public void setInterfaceTimeout (int _interfaceTimeout) {
    this._interfaceTimeout = _interfaceTimeout;
  }

  // / <summary>
  // / The # of minutes the student can be idle after making a print request
  // before logging them out.
  // / </summary>
  @JsonProperty ("requestInterfaceTimeout")
  public int getRequestInterfaceTimeout () {
    return _requestInterfaceTimeout;
  }

  public void setRequestInterfaceTimeout (int _requestInterfaceTimeout) {
    this._requestInterfaceTimeout = _requestInterfaceTimeout;
  }

  // / <summary>
  // / The # of minutes until a pause will expire answered questions.
  // / </summary>
  @JsonProperty ("oppRestartMins")
  public int getOppRestartMins () {
    return _oppRestartMins;
  }

  public void setOppRestartMins (int _oppRestartMins) {
    this._oppRestartMins = _oppRestartMins;
  }

  @JsonProperty ("prefetch")
  public int getPrefetch () {
    return _prefetch;
  }

  public void setPrefetch (int _prefetch) {
    this._prefetch = _prefetch;
  }

  @JsonProperty ("scoreByTDS")
  public boolean isScoreByTDS () {
    return _scoreByTDS;
  }

  public void setScoreByTDS (boolean _scoreByTDS) {
    this._scoreByTDS = _scoreByTDS;
  }

  @JsonProperty ("validateCompleteness")
  public boolean isValidateCompleteness () {
    return _validateCompleteness;
  }

  public void setValidateCompleteness (boolean _validateCompleteness) {
    this._validateCompleteness = _validateCompleteness;
  }

}
