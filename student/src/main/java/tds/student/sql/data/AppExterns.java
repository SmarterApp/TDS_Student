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

import org.apache.commons.lang.time.DateUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import AIR.Common.Utilities.TDSStringUtils;

/**
 * @author temp_rreddy
 * 
 */
public class AppExterns
{
  private String _clientName;
  private String _environment;
  private String _clientStylePath;
  private int    _timeZoneOffset;
  private String _itemBankDB;
  private String _testeeCheckin;
  private String _appName;

  public AppExterns () {
  }

  // TODO
  public Date convertEST_XST (Date sourceDateTime) {
    return DateUtils.addHours (sourceDateTime, getTimeZoneOffset () * -1);
  }

  public Date convertXST_EST (Date sourceDateTime) {
    return DateUtils.addHours (sourceDateTime, getTimeZoneOffset ());
  }

  @JsonProperty ("ClientName")
  public String getClientName () {
    return _clientName;
  }

  public void setClientName (String _clientName) {
    this._clientName = _clientName;
  }

  @JsonProperty ("Environment")
  public String getEnvironment () {
    return _environment;
  }

  public void setEnvironment (String _environment) {
    this._environment = _environment;
  }

  @JsonProperty ("ClientStylePath")
  public String getClientStylePath () {
    return _clientStylePath;
  }

  public void setClientStylePath (String _clientStylePath) {
    this._clientStylePath = _clientStylePath;
  }

  @JsonProperty ("TimeZoneOffset")
  public int getTimeZoneOffset () {
    return _timeZoneOffset;
  }

  public void setTimeZoneOffset (int _timeZoneOffset) {
    this._timeZoneOffset = _timeZoneOffset;
  }

  @JsonProperty ("ItemBankDB")
  public String getItemBankDB () {
    return _itemBankDB;
  }

  public void setItemBankDB (String _itemBankDB) {
    this._itemBankDB = _itemBankDB;
  }

  @JsonProperty ("TesteeCheckin")
  public String getTesteeCheckin () {
    return _testeeCheckin;
  }

  public void setTesteeCheckin (String _testeeCheckin) {
    this._testeeCheckin = _testeeCheckin;
  }

  @JsonProperty ("AppName")
  public String getAppName () {
    return _appName;
  }

  public void setAppName (String _appName) {
    this._appName = _appName;
  }

  /**
   * @return the _systemName
   */
  @JsonProperty ("SystemName")
  public String getSystemName () {
    return String.format ("%1$s_%2$s_%3$s", getClientName (), getEnvironment (), getAppName ());
  }

}
