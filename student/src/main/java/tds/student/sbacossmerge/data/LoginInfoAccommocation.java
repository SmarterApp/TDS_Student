/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *       
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sbacossmerge.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginInfoAccommocation
{
  @JsonProperty ("type")
  private String       _type;

  @JsonProperty ("codes")
  private List<String> _codes;

  public String getType () {
    return _type;
  }

  public void setType (String _type) {
    this._type = _type;
  }
}
