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
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestForm
{
  private String _key;
  private String _id;

  @JsonProperty ("key")
  public String getKey () {
    return _key;
  }

  public void setKey (String _key) {
    this._key = _key;
  }

  @JsonProperty ("id")
  public String getId () {
    return _id;
  }

  public void setId (String _id) {
    this._id = _id;
  }

  public TestForm () {
  }

  public TestForm (String key, String id) {
    _key = key;
    _id = id;
  }

}
