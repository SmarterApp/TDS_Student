/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
public class NextItemGroupResult
{
  public PageGroup _page;
  public long      _dbLatency;

  /**
   * @return the _page
   */
  @JsonProperty ("Page")
  public PageGroup getPage () {
    return _page;
  }

  /**
   * @param _page
   *          the _page to set
   */
  public void setPage (PageGroup _page) {
    this._page = _page;
  }

  /**
   * @return the _dbLatency
   */
  @JsonProperty ("DbLatency")
  public long getDbLatency () {
    return _dbLatency;
  }

  /**
   * @param _dbLatency
   *          the _dbLatency to set
   */
  public void setDbLatency (long _dbLatency) {
    this._dbLatency = _dbLatency;
  }

  public NextItemGroupResult (PageGroup page)
  {
    _page = page;
  }
}
