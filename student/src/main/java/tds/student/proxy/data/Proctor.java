/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.proxy.data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import tds.student.sql.data.TestSession;

/**
 * @author temp_rreddy
 * 
 */
public class Proctor
{
  private boolean _isAuth = false; // is authenticated

  public String   _id;

  public UUID     _browserKey;

  // / <summary>
  // / The proctor user key.
  // / </summary>
  public long     _key;

  // / <summary>
  // / The RTS entity key.
  // / </summary>
  public long     _entityKey;

  // / <summary>
  // / The full name of the proctor.
  // / </summary>

  public String   _fullname;

  public String   _rtsPassword;

  // / <summary>
  // / The RTS user role.
  // / </summary>
  public String   _role;

  // the client name for which this proctor is authenticated.
  public String   _clientName;

  // session information - if one has been created.
  public UUID     _sessionKey;
  public String   _sessionId;
  public String   _sessionName;

  public Proctor (String id, UUID browserKey)
  {
    _id = id;
    _browserKey = browserKey;
    // initialize empty session.
    _sessionKey = UUID.randomUUID ();
    _sessionName = "";
    _sessionId = "";
  }

  /**
   * @return the isAuth
   */
  @JsonProperty ("IsAuth")
  public boolean isAuth () {
    return _isAuth;
  }

  /**
   * @param isAuth
   *          the isAuth to set
   */
  public void setAuth (boolean isAuth) {
    this._isAuth = isAuth;
  }

  /**
   * @return the _id
   */
  @JsonProperty ("id")
  public String getId () {
    return _id;
  }

  /**
   * @param _id
   *          the _id to set
   */
  public void setId (String _id) {
    this._id = _id;
  }

  /**
   * @return the _browserKey
   */
  @JsonProperty ("BrowserKey")
  public UUID getBrowserKey () {
    return _browserKey;
  }

  /**
   * @param _browserKey
   *          the _browserKey to set
   */
  public void setBrowserKey (UUID _browserKey) {
    this._browserKey = _browserKey;
  }

  /**
   * @return the _key
   */
  @JsonProperty ("Key")
  public long getKey () {
    return _key;
  }

  /**
   * @param _key
   *          the _key to set
   */
  public void setKey (long _key) {
    this._key = _key;
  }

  /**
   * @return the _entityKey
   */
  @JsonProperty ("EntityKey")
  public long getEntityKey () {
    return _entityKey;
  }

  /**
   * @param _entityKey
   *          the _entityKey to set
   */
  public void setEntityKey (long _entityKey) {
    this._entityKey = _entityKey;
  }

  /**
   * @return the _fullname
   */
  @JsonProperty ("fullName")
  public String getFullname () {
    return _fullname;
  }

  /**
   * @param _fullname
   *          the _fullname to set
   */
  public void setFullname (String _fullname) {
    this._fullname = _fullname;
  }

  /**
   * @return the _rtsPassword
   */
  @JsonProperty ("RTSPassword")
  public String getRtsPassword () {
    return _rtsPassword;
  }

  /**
   * @param _rtsPassword
   *          the _rtsPassword to set
   */
  public void setRtsPassword (String _rtsPassword) {
    this._rtsPassword = _rtsPassword;
  }

  /**
   * @return the _role
   */
  @JsonProperty ("Role")
  public String getRole () {
    return _role;
  }

  /**
   * @param _role
   *          the _role to set
   */
  public void setRole (String _role) {
    this._role = _role;
  }

  /**
   * @return the _clientName
   */
  @JsonProperty ("ClientName")
  public String getClientName () {
    return _clientName;
  }

  /**
   * @param _clientName
   *          the _clientName to set
   */
  public void setClientName (String _clientName) {
    this._clientName = _clientName;
  }

  /**
   * @return the _sessionKey
   */
  @JsonProperty ("SessionKey")
  public UUID getSessionKey () {
    return _sessionKey;
  }

  /**
   * @param _sessionKey
   *          the _sessionKey to set
   */
  public void setSessionKey (UUID _sessionKey) {
    this._sessionKey = _sessionKey;
  }

  /**
   * @return the _sessionId
   */
  @JsonProperty ("SessionId")
  public String getSessionId () {
    return _sessionId;
  }

  /**
   * @param _sessionId
   *          the _sessionId to set
   */
  public void setSessionId (String _sessionId) {
    this._sessionId = _sessionId;
  }

  /**
   * @return the _sessionName
   */
  @JsonProperty ("SessionName")
  public String getSessionName () {
    return _sessionName;
  }

  /**
   * @param _sessionName
   *          the _sessionName to set
   */
  public void setSessionName (String _sessionName) {
    this._sessionName = _sessionName;
  }

  public SessionInstance CreateSessionInstance (UUID sessionKey)
  {
    return new SessionInstance (sessionKey, _key, _browserKey);
  }

  public TestSession GetSessionInfo ()
  {
    TestSession session = new TestSession ();
    session.setKey (_sessionKey);
    session.setName (_sessionName);
    session.setId (_sessionId);
    return session;
  }

  public void SetSessionInfo (TestSession session)
  {
    this._sessionKey = session.getKey ();
    this._sessionName = session.getName ();
    this._sessionId = session.getId ();
  }

}
