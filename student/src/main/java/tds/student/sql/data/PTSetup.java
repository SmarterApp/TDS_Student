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
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PTSetup
{
  private String  _loginAs;
  private String  _firstName;
  private UUID    _sessionKey;
  private String  _sessionID;
  private boolean _inPTMode;

  @JsonProperty ("loginAs")
  public String getLoginAs () {
    return _loginAs;
  }

  public void setLoginAs (String _loginAs) {
    this._loginAs = _loginAs;
  }

  @JsonProperty ("firstName")
  public String getFirstName () {
    return _firstName;
  }

  public void setFirstName (String _firstName) {
    this._firstName = _firstName;
  }

  public UUID getSessionKey () {
    return _sessionKey;
  }

  public void setSessionKey (UUID _sessionKey) {
    this._sessionKey = _sessionKey;
  }

  @JsonProperty ("sessionID")
  public String getSessionID () {
    return _sessionID;
  }

  public void setSessionID (String _sessionID) {
    this._sessionID = _sessionID;
  }
  @JsonProperty ("InPTMode")
  public boolean inPTMode () {
    // TODO
    return "GUEST".equalsIgnoreCase (_loginAs);
  }

}
