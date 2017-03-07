/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.util.UUID;

import TDS.Shared.Data.DataUtils;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author temp_rreddy
 * 
 */
public class ItemResponseScorable implements IItemResponseScorable
{	
  private String _testKey;
  private String _testID;
  private String _language;
  private int    _position;
  private int    _sequence;
  private long   _bankKey;
  private long   _itemKey;
  private String _segmentID;
  private String _value;
  private UUID   _scoreMark;
  private String _filePath;
  private String clientName;

  public ItemResponseScorable () {
  }

  public ItemResponseScorable (String testKey, String testID, String language, int position, int sequence, long bankKey, long itemKey, String segmentID, String value, UUID scoreMark, String itemFilePath) {
    this(testKey, testID, language, position, sequence, bankKey, itemKey, segmentID, value, scoreMark, itemFilePath, "");
  }

  public ItemResponseScorable(String _testKey, String _testID, String _language, int _position, int _sequence, long _bankKey, long _itemKey, String _segmentID, String _value, UUID _scoreMark, String _filePath, String clientName) {
    this._testKey = _testKey;
    this._testID = _testID;
    this._language = _language;
    this._position = _position;
    this._sequence = _sequence;
    this._bankKey = _bankKey;
    this._itemKey = _itemKey;
    this._segmentID = _segmentID;
    this._value = _value;
    this._scoreMark = _scoreMark;
    this._filePath = _filePath;
    this.clientName = clientName;
  }

  @Override
  @JsonProperty ("TestKey")
  public String getTestKey () {
    return _testKey;
  }

  public void setTestKey (String _testKey) {
    this._testKey = _testKey;
  }
  
  @Override
  @JsonProperty ("Language")
  public String getLanguage () {
    return _language;
  }

  public void setLanguage (String _language) {
    this._language = _language;
  }

  @Override
  @JsonProperty ("Position")
  public int getPosition () {
    return _position;
  }

  public void setPosition (int _position) {
    this._position = _position;
  }

  @Override
  @JsonProperty ("Sequence")
  public int getSequence () {
    return _sequence;
  }

  public void setSequence (int _sequence) {
    this._sequence = _sequence;
  }

  @Override
  @JsonProperty ("BankKey")
  public long getBankKey () {
    return _bankKey;
  }

  public void setBankKey (long _bankKey) {
    this._bankKey = _bankKey;
  }

  @Override
  @JsonProperty ("ItemKey")
  public long getItemKey () {
    return _itemKey;
  }

  public void setItemKey (long _itemKey) {
    this._itemKey = _itemKey;
  }

  @Override
  @JsonProperty ("SegmentID")
  public String getSegmentID () {
    return _segmentID;
  }

  public void setSegmentID (String _segmentID) {
    this._segmentID = _segmentID;
  }

  @JsonProperty ("ItemID")
  public String getItemID () {
	return DataUtils.makeItemID (_bankKey, _itemKey);
  }

  @Override
  @JsonProperty ("Value")
  public String getValue () {
    return _value;
  }

  public void setValue (String _value) {
    this._value = _value;
  }

  @Override
  @JsonProperty ("ScoreMark")
  public UUID getScoreMark () {
    return _scoreMark;
  }

  public void setScoreMark (UUID _scoreMark) {
    this._scoreMark = _scoreMark;
  }

  @Override
  @JsonProperty ("FilePath")
  public String getFilePath () {
    return _filePath;
  }

  public void setFilePath (String _filePath) {
	  this._filePath = _filePath;
  }

  @Override
  @JsonProperty ("TestID")
  public String getTestID() {
	  return _testID;
  }

  public void setTestID(String testID) {
	  this._testID = testID;
  }

  @Override
  public String getClientName() {
    return clientName;
  }
}
