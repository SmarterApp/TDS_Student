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
public class BrowserCapabilities
{
  private String  _clientIP;
  private String  _proxyIP;
  private String  _userAgent;
  private String  _screenRez;
  private boolean _isSecure;
  private String  _macAddress;
  private String  _localIP;
  private String  _textToSpeech;

  @JsonProperty ("ClientIP")
  public String getClientIP () {
    return _clientIP;
  }

  public void setClientIP (String _clientIP) {
    this._clientIP = _clientIP;
  }

  @JsonProperty ("ProxyIP")
  public String getProxyIP () {
    return _proxyIP;
  }

  public void setProxyIP (String _proxyIP) {
    this._proxyIP = _proxyIP;
  }

  @JsonProperty ("UserAgent")
  public String getUserAgent () {
    return _userAgent;
  }

  public void setUserAgent (String _userAgent) {
    this._userAgent = _userAgent;
  }

  @JsonProperty ("ScreenRez")
  public String getScreenRez () {
    return _screenRez;
  }

  public void setScreenRez (String _screenRez) {
    this._screenRez = _screenRez;
  }

  @JsonProperty ("IsSecure")
  public boolean isSecure () {
    return _isSecure;
  }

  public void setIsSecure (boolean _isSecure) {
    this._isSecure = _isSecure;
  }

  @JsonProperty ("MacAddress")
  public String getMacAddress () {
    return _macAddress;
  }

  public void setMacAddress (String _macAddress) {
    this._macAddress = _macAddress;
  }

  @JsonProperty ("LocalIP")
  public String getLocalIP () {
    return _localIP;
  }

  public void setLocalIP (String _localIP) {
    this._localIP = _localIP;
  }

  @JsonProperty ("TextToSpeech")
  public String getTextToSpeech () {
    return _textToSpeech;
  }

  public void setTextToSpeech (String _textToSpeech) {
    this._textToSpeech = _textToSpeech;
  }

}
