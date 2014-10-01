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

public class ToolUsed
{

  private int    _page;
  private String _type;
  private String _code;
  private int    _count;

  @JsonProperty ("page")
  public int getPage () {
    return _page;
  }

  public void setPage (int _page) {
    this._page = _page;
  }

  @JsonProperty ("type")
  public String getType () {
    return _type;
  }

  public void setType (String _type) {
    this._type = _type;
  }

  @JsonProperty ("code")
  public String getCode () {
    return _code;
  }

  public void setCode (String _code) {
    this._code = _code;
  }

  @JsonProperty ("count")
  public int getCount () {
    return _count;
  }

  public void setCount (int _count) {
    this._count = _count;
  }

}
