/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.data;

import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.TestConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
public class TestOpportunity
{
  private final OpportunityInstance _oppInstance;
  private final String              _testKey;
  private final String				_testID;
  private final String              _language;
  private final TestConfig          _testConfig;

  /**
   * @return the _oppInstance
   */
  @JsonProperty ("OppInstance")
  public OpportunityInstance getOppInstance () {
    return _oppInstance;
  }

  /**
   * @return the _testKey
   */
  @JsonProperty ("TestKey")
  public String getTestKey () {
    return _testKey;
  }

  /**
   * @return the _testID
   */
  @JsonProperty ("TestID")
  public String getTestID () {
    return _testID;
  }

  /**
   * @return the _language
   */
  @JsonProperty ("Language")
  public String getLanguage () {
    return _language;
  }
  
  /**
   * @return the _testConfig
   */
  @JsonProperty ("Config")
  public TestConfig getTestConfig () {
    return _testConfig;
  }

  public TestOpportunity (OpportunityInstance oppInstance, String testKey, String testID, String language, TestConfig testConfig)
  {
    _oppInstance = oppInstance;
    _testKey = testKey;
    _testID = testID;
    _language = language;
    _testConfig = testConfig;
  }

}
