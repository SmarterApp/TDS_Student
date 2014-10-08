/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
public class TestDisplayScores extends ArrayList<TestDisplayScore>
{
  private boolean _isCompleted;
  private boolean _isScored;
  private boolean _scoreByTDS;
  private boolean _showScores;

  @JsonProperty ("IsCompleted")
  public boolean isCompleted () {
    return _isCompleted;
  }

  @JsonProperty ("IsScored")
  public boolean isScored () {
    return _isScored;
  }

  public boolean isScoreByTDS () {
    return _scoreByTDS;
  }

  public void setScoreByTDS (boolean _scoreByTDS) {
    this._scoreByTDS = _scoreByTDS;
  }

  public boolean isShowScores () {
    return _showScores;
  }

  public void setShowScores (boolean _showScores) {
    this._showScores = _showScores;
  }

  public void SetScoreStatus (String scoreStatus) {
    _isCompleted = (scoreStatus == null || scoreStatus.equals("COMPLETED: Do Not Score") || scoreStatus.equals("Unofficial score only") || scoreStatus.equals("SCORED"));

    _isScored = ("SCORED".equals (scoreStatus));
  }
}
