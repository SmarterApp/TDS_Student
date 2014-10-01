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

import com.fasterxml.jackson.annotation.JsonProperty;

import AIR.Common.Helpers.StopWatch;
import AIR.Common.Web.Session.HttpContext;

/**
 * @author temp_rreddy
 * 
 */
public class ServerLatency
{
  private final Date   _dateCreated;
  private StopWatch    _stopwatchServer;
  private String       _pageList;
  private String       _itemList;
  public OperationType _operation;
  public long          _latency;
  public long          _dbLatency;

  public ServerLatency () {
    _dateCreated = new Date ();
    _operation = OperationType.Unknown;
  }

  public static String getRequestTypeValue (OperationType operationType) {
    return operationType.toString ().toLowerCase ();
  }

  public Date getDateCreated () {
    return _dateCreated;
  }

  public String getOperationName () {

    return getRequestTypeValue (this._operation);
  }

  /**
   * @return the _operation
   */
  @JsonProperty ("Operation")
  public OperationType getOperation () {
    return _operation;
  }

  /**
   * @param _operation the _operation to set
   */
  public void setOperation (OperationType _operation) {
    this._operation = _operation;
  }

  /**
   * @return the _latency
   */
  public long getLatency () {
    // TODO mpatel/Shiva ElapsedMilliseconds equivalent in java
    // return (_stopwatchServer == null) ? -1 : _stopwatchServer.ElapsedMilliseconds;
    return (_stopwatchServer == null) ? -1 : _stopwatchServer.getTime ();
  }

  public long getDbLatency () {
    return _dbLatency;
  }

  public void setDbLatency (long _dbLatency) {
    this._dbLatency = _dbLatency;
  }

  public void StartTimer () {
    if (_stopwatchServer == null) {
      _stopwatchServer = new StopWatch ();
    }

    _stopwatchServer.start ();
  }

  public void stopTimer () {
    if (_stopwatchServer != null && _stopwatchServer.isRunning ()) {
      _stopwatchServer.stop ();
    }
  }

  public enum OperationType {
    Unknown, Selection, Update, Score
  }

  //TODO mpatel/Shiva - Check with Shiva This method was accepting HttpContyextWrapper
  public static ServerLatency getCurrent (HttpContext context) {
    if (context == null)
      return null;

    ServerLatency latency = (ServerLatency)context.getItem("Latency");

    if (latency == null) {
      latency = new ServerLatency ();
      context.setItem ("Latency",latency);
    }

    return latency;
  }

  @JsonProperty ("PageList")
  public String getPageList () {
    return _pageList;
  }

  public void setPageList (String _pageList) {
    this._pageList = _pageList;
  }

  @JsonProperty ("ItemList")
  public String getItemList () {
    return _itemList;
  }

  public void setItemList (String _itemList) {
    this._itemList = _itemList;
  }

}
