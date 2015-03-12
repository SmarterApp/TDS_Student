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

public class OpportunityInstance
{
  private final UUID _oppKey;
  private final UUID _sessionKey;
  private final UUID _browserKey;

  @JsonProperty ("Key")
  public UUID getKey () {
    return _oppKey;
  }

  @JsonProperty ("SessionKey")
  public UUID getSessionKey () {
    return _sessionKey;
  }

  @JsonProperty ("BrowserKey")
  public UUID getBrowserKey () {
    return _browserKey;
  }

  public OpportunityInstance (UUID oppKey, UUID sessionKey, UUID browserKey) {
    _oppKey = oppKey;
    _sessionKey = sessionKey;
    _browserKey = browserKey;
  }
}