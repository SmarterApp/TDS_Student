/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
public class ItemDisplayScore
{
  private int    _position;
  private int    _page;
  private String _format;
  private String _response;
  // / <summary>
  // / The students score
  // / </summary>
  private int    _score;
  // / <summary>
  // / The max number of points.
  // / </summary>
  private int    _scoreMax;
  private String _scoreRationale;

  @JsonProperty ("position")
  public int getPosition () {
    return _position;
  }

  public void setPosition (int _position) {
    this._position = _position;
  }

  @JsonProperty ("page")
  public int getPage () {
    return _page;
  }

  public void setPage (int _page) {
    this._page = _page;
  }

  @JsonProperty ("format")
  public String getFormat () {
    return _format;
  }

  public void setFormat (String _format) {
    this._format = _format;
  }

  @JsonProperty ("response")
  public String getResponse () {
    return _response;
  }

  public void setResponse (String _response) {
    this._response = _response;
  }

  @JsonProperty ("score")
  public int getScore () {
    return _score;
  }

  public void setScore (int _score) {
    this._score = _score;
  }

  @JsonProperty ("scoreMax")
  public int getScoreMax () {
    return _scoreMax;
  }

  public void setScoreMax (int _scoreMax) {
    this._scoreMax = _scoreMax;
  }

  @JsonProperty ("scoreRationale")
  public String getScoreRationale () {
    return _scoreRationale;
  }

  public void setScoreRationale (String _scoreRationale) {
    this._scoreRationale = _scoreRationale;
  }

}
