/*************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *************************************************************************/

package tds.student.data;

import java.util.List;

import tds.student.sql.data.TestSegment;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mskhan
 * 
 */
public class TestInfo
{
  // PROPERTIES:

  private String        _urlBase;

  private boolean       _hasAudio;

  private boolean       _autoMute;

  private int           _reviewPage;

  // CONFIG:

  private String        _testName;

  private int           _testLength;

  private int           _startPosition;

  private int           _contentLoadTimeout;

  private int           _requestInterfaceTimeout;

  private int           _oppRestartMins;

  private int           _interfaceTimeout;

  private int           _prefetch;

  private boolean       _validateCompleteness;

  // APP SETTINGS:

  private int           _interfaceTimeoutDialog;

  private int           _autoSaveInterval;

  private int           _forbiddenAppsInterval;

  private boolean       _disableSaveWhenInactive;

  private boolean       _disableSaveWhenForbiddenApps;

  private boolean       _allowSkipAudio;

  private boolean       _showSegmentLabels;

  private int           _audioTimeout;

  private boolean       _enableLogging;

  private String        _dictionaryUrl;

  private List<Segment> _segments;

  // TDS.Comments = [];
  private List<String>  _comments;

  @JsonProperty ("urlBase")
  public String getUrlBase () {
    return _urlBase;
  }

  public void setUrlBase (String value) {
    this._urlBase = value;
  }

  @JsonProperty ("hasAudio")
  public boolean isHasAudio () {
    return _hasAudio;
  }

  public void setHasAudio (boolean value) {
    this._hasAudio = value;
  }

  @JsonProperty ("autoMute")
  public boolean isAutoMute () {
    return _autoMute;
  }

  public void setAutoMute (boolean value) {
    this._autoMute = value;
  }

  @JsonProperty ("reviewPage")
  public int getReviewPage () {
    return _reviewPage;
  }

  public void setReviewPage (int value) {
    this._reviewPage = value;
  }

  @JsonProperty ("testName")
  public String getTestName () {
    return _testName;
  }

  public void setTestName (String value) {
    this._testName = value;
  }

  @JsonProperty ("testLength")
  public int getTestLength () {
    return _testLength;
  }

  public void setTestLength (int value) {
    this._testLength = value;
  }

  @JsonProperty ("startPosition")
  public int getStartPosition () {
    return _startPosition;
  }

  public void setStartPosition (int value) {
    this._startPosition = value;
  }

  @JsonProperty ("contentLoadTimeout")
  public int getContentLoadTimeout () {
    return _contentLoadTimeout;
  }

  public void setContentLoadTimeout (int value) {
    this._contentLoadTimeout = value;
  }

  @JsonProperty ("requestInterfaceTimeout")
  public int getRequestInterfaceTimeout () {
    return _requestInterfaceTimeout;
  }

  public void setRequestInterfaceTimeout (int value) {
    this._requestInterfaceTimeout = value;
  }

  @JsonProperty ("oppRestartMins")
  public int getOppRestartMins () {
    return _oppRestartMins;
  }

  public void setOppRestartMins (int value) {
    this._oppRestartMins = value;
  }

  @JsonProperty ("interfaceTimeout")
  public int getInterfaceTimeout () {
    return _interfaceTimeout;
  }

  public void setInterfaceTimeout (int value) {
    this._interfaceTimeout = value;
  }

  @JsonProperty ("prefetch")
  public int getPrefetch () {
    return _prefetch;
  }

  public void setPrefetch (int value) {
    this._prefetch = value;
  }

  @JsonProperty ("validateCompleteness")
  public boolean isValidateCompleteness () {
    return _validateCompleteness;
  }

  public void setValidateCompleteness (boolean value) {
    this._validateCompleteness = value;
  }

  @JsonProperty ("interfaceTimeoutDialog")
  public int getInterfaceTimeoutDialog () {
    return _interfaceTimeoutDialog;
  }

  public void setInterfaceTimeoutDialog (int value) {
    this._interfaceTimeoutDialog = value;
  }

  @JsonProperty ("autoSaveInterval")
  public int getAutoSaveInterval () {
    return _autoSaveInterval;
  }

  public void setAutoSaveInterval (int value) {
    this._autoSaveInterval = value;
  }

  @JsonProperty ("forbiddenAppsInterval")
  public int getForbiddenAppsInterval () {
    return _forbiddenAppsInterval;
  }

  public void setForbiddenAppsInterval (int value) {
    _forbiddenAppsInterval = value;
  }

  @JsonProperty ("disableSaveWhenInactive")
  public boolean isDisableSaveWhenInactive () {
    return _disableSaveWhenInactive;
  }

  public void setDisableSaveWhenInactive (boolean value) {
    _disableSaveWhenInactive = value;
  }

  @JsonProperty ("disableSaveWhenForbiddenApps")
  public boolean isDisableSaveWhenForbiddenApps () {
    return _disableSaveWhenForbiddenApps;
  }

  public void setDisableSaveWhenForbiddenApps (boolean value) {
    this._disableSaveWhenForbiddenApps = value;
  }

  @JsonProperty ("allowSkipAudio")
  public boolean isAllowSkipAudio () {
    return _allowSkipAudio;
  }

  public void setAllowSkipAudio (boolean value) {
    this._allowSkipAudio = value;
  }

  @JsonProperty ("showSegmentLabels")
  public boolean isShowSegmentLabels () {
    return _showSegmentLabels;
  }

  public void setShowSegmentLabels (boolean value) {
    this._showSegmentLabels = value;
  }

  @JsonProperty ("audioTimeout")
  public int getAudioTimeout () {
    return _audioTimeout;
  }

  public void setAudioTimeout (int value) {
    this._audioTimeout = value;
  }

  @JsonProperty ("enableLogging")
  public boolean isEnableLogging () {
    return _enableLogging;
  }

  public void setEnableLogging (boolean value) {
    this._enableLogging = value;
  }

  @JsonProperty ("dictionaryUrl")
  public String getDictionaryUrl () {
    return _dictionaryUrl;
  }

  public void setDictionaryUrl (String value) {
    this._dictionaryUrl = value;
  }

  @JsonProperty ("comments")
  public List<String> getComments () {
    return _comments;
  }

  public void setComments (List<String> value) {
    this._comments = value;
  }

  @JsonProperty ("segments")
  public List<Segment> getSegments () {
    return _segments;
  }

  public void setSegments (List<Segment> value) {
    this._segments = value;
  }
}
