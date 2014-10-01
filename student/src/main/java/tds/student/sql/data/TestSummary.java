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

public class TestSummary
{
  private TestDisplayScores _testScores;
  private ItemDisplayScores _itemScores;
  private boolean           _viewResponses;
  private boolean           _pollForScores;

  @JsonProperty ("testScores")
  public TestDisplayScores getTestScores () {
    return _testScores;
  }

  public void setTestScores (TestDisplayScores _testScores) {
    this._testScores = _testScores;
  }

  @JsonProperty ("itemScores")
  public ItemDisplayScores getItemScores () {
    return _itemScores;
  }

  public void setItemScores (ItemDisplayScores _itemScores) {
    this._itemScores = _itemScores;
  }

  @JsonProperty ("viewResponses")
  public boolean isViewResponses () {
    return _viewResponses;
  }

  public void setViewResponses (boolean _viewResponses) {
    this._viewResponses = _viewResponses;
  }

  @JsonProperty ("pollForScores")
  public boolean isPollForScores () {
    return _pollForScores;
  }

  public void setPollForScores (boolean _pollForScores) {
    this._pollForScores = _pollForScores;
  }

}
