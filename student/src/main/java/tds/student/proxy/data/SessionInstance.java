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

/**
 * @author temp_rreddy
 * 
 */
// / <summary>
// / A collection of keys that represents an instance of a session.
// / </summary>
public class SessionInstance
{
  // / <summary>
  // / Session key.
  // / </summary>
  public UUID _key;

  // / <summary>
  // / Proctor key.
  // / </summary>
  public long _proctorKey;

  // / <summary>
  // / Browser key.
  // / </summary>
  public UUID _browserKey;

  public SessionInstance (UUID key, long proctorKey, UUID browserKey)
  {
    _key = key;
    _proctorKey = proctorKey;
    _browserKey = browserKey;
  }

  /**
   * @return the _key
   */
  @JsonProperty ("Key")
  public UUID getKey () {
    return _key;
  }

  /**
   * @param _key
   *          the _key to set
   */
  private void setKey (UUID _key) {
    this._key = _key;
  }

  /**
   * @return the _proctorKey
   */
  @JsonProperty ("ProctorKey")
  public long getProctorKey () {
    return _proctorKey;
  }

  /**
   * @param _proctorKey
   *          the _proctorKey to set
   */
  private void setProctorKey (long _proctorKey) {
    this._proctorKey = _proctorKey;
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
  private void setBrowserKey (UUID _browserKey) {
    this._browserKey = _browserKey;
  }

}
