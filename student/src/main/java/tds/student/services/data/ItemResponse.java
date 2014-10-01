/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.data;

import java.util.UUID;

import tds.itemrenderer.data.IITSDocument;
import tds.student.sql.data.IItemResponseUpdate;
import tds.student.sql.data.OpportunityItem;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
//[DebuggerDisplay("Item:{ItemID}, Position:{Position}, Page:{Page}")]
public class ItemResponse implements IItemResponseUpdate
{
  private long _bankKey;
  private long _itemKey;
  private int     _segment;
  private String  _segmentID;
  private int     _page;
  private int     _position;
  private String  _dateCreated;
  private String  _format;
  private boolean _markForReview;
  private int     _sequence;
  private String  _value;

  /** Was this the selected item(s) in a group.
   * Some groups have all the items selected (e.x. MC) and others should only
   * have one (e.x. Writing).
   */
  public boolean _isSelected;

  /** Is this item required to be answered.
   */
  public boolean _isRequired;

  /** The max score points for if you get this item correct.
   */
  public int     _scorePoint;

  /** Is this response visible to the testee and in an interactive state.
   */
  public boolean _isVisible;
  
  /** Is the response considered valid. You can use this information to
   * determine if a group is completed.
   * The client dictates this information.
   */
  public boolean _isValid;

  public ItemResponse (OpportunityItem response)
  {
    _bankKey = response.getBankKey ();
    _itemKey = response.getItemKey ();
    _segment = response.getSegment ();
    _segmentID = response.getSegmentID ();
    _page = response.getPage ();
    _position = response.getPosition ();
    _dateCreated = response.getDateCreated ();
    _format = response.getFormat ();
    _isRequired = response.isRequired ();

    _isSelected = response.getIsSelected ();
    _isValid = response.getIsValid ();
    _sequence = response.getSequence ();
    _isVisible = response.isVisible ();
    _markForReview = response.isMarkForReview ();
    _value = response.getValue ();
    _filePath = response.getItemFile ();
    _isPrintable = response.isPrintable ();
  }
  
  /**
   * @return the _bankKey
   */
  @JsonProperty ("BankKey")
  public long getBankKey () {
    return _bankKey;
  }


  /**
   * @return the _itemKey
   */
  @JsonProperty ("ItemKey")
  public long getItemKey () {
    return _itemKey;
  }

  /**
   * @param _itemKey
   *          the _itemKey to set
   */
  public void setItemKey (long _itemKey) {
    this._itemKey = _itemKey;
  }

  @Override
  @JsonProperty("ItemID")
  public String getItemID ()
  {
    return String.format ("%1$d-%2$d", getBankKey (), getItemKey ());
  }

  /**
   * @return the _segment
   */
  @JsonProperty ("Segment")
  public int getSegment () {
    return _segment;
  }


  /**
   * @return the _segmentID
   */
  @JsonProperty ("SegmentID")
  public String getSegmentID () {
    return _segmentID;
  }

  /**
   * @return the _page
   */
  @Override
  @JsonProperty ("Page")
  public int getPage () {
    return _page;
  }


  /**
   * @return the _position
   */
  @Override
  @JsonProperty ("Position")
  public int getPosition () {
    return _position;
  }


  /**
   * @return the _dateCreated
   */
  @Override
  @JsonProperty ("DateCreated")
  public String getDateCreated () {
    return _dateCreated;
  }

  /**
   * @return the _format
   */
  @JsonProperty ("Format")
  public String getFormat () {
    return _format;
  }


  /**
   * @return the _markForReview
   */
  @JsonProperty ("MarkForReview")
  public boolean isMarkForReview () {
    return _markForReview;
  }


  /**
   * @return the _sequence
   */
  @Override
  @JsonProperty ("Sequence")
  public int getSequence () {
    return _sequence;
  }

  /**
   * @param _sequence
   *          the _sequence to set
   */
  public void setSequence (int _sequence) {
    this._sequence = _sequence;
  }

  /**
   * @return the _value
   */
  @Override
  @JsonProperty ("Value")
  public String getValue () {
    return _value;
  }

  /**
   * @param _value
   *          the _value to set
   */
  public void setValue (String _value) {
    this._value = _value;
  }

  /**
   * @return the _isSelected
   */
  @JsonProperty ("IsSelected")
  public boolean getIsSelected () {
    return _isSelected;
  }

  /**
   * @param _isSelected
   *          the _isSelected to set
   */
  public void setIsSelected (boolean _isSelected) {
    this._isSelected = _isSelected;
  }

  /**
   * @return the _isRequired
   */
  @JsonProperty ("IsRequired")
  public boolean isRequired () {
    return _isRequired;
  }


  /**
   * @return the _scorePoint
   */
  @JsonProperty ("ScorePoint")
  public int getScorePoint () {
    return _scorePoint;
  }


  /**
   * @return the _isVisible
   */
  @JsonProperty ("IsVisible")
  public boolean isVisible () {
    return _isVisible;
  }

  /**
   * @param _isVisible
   *          the _isVisible to set
   */
  public void setIsVisible (boolean _isVisible) {
    this._isVisible = _isVisible;
  }

  /**
   * @return the _isValid
   */
  @Override
  @JsonProperty ("IsValid")
  public boolean getIsValid () {
    return _isValid;
  }

  /**
   * @param _isValid
   *          the _isValid to set
   */
  public void setIsValid (boolean _isValid) {
    this._isValid = _isValid;
  }

  // / <summary>
  // / Determine if this response has been answered.
  // / </summary>
  // / <remarks>
  // / To be answered the response has to be selected and determined to be
  // valid.
  // / </remarks>
  @JsonProperty ("Answered")
  public boolean getAnswered ()
  {
    return (_isSelected && _isValid);
  }

  // / <summary>
  // / Was this item prefetched.
  // / </summary>
  public boolean _prefetched;
  // / <summary>
  // / Is this item printable.
  // / </summary>
  // / <remarks>
  // / This is kind of hacky because this property only gets set
  // / when calling T_GetItemGroup. Otherwise it will always be false.
  // / </remarks>
  public boolean _isPrintable;
  // / <summary>
  // / Was the item printed on the server before getting sent down to the
  // client.
  // / </summary>
  // / <remarks>
  // / This can happen for Braille items.
  // / </remarks>

  public boolean _printed;

  /**
   * @return the _prefetched
   */
  @JsonProperty ("Prefetched")
  public boolean isPrefetched () {
    return _prefetched;
  }

  /**
   * @param _prefetched
   *          the _prefetched to set
   */
  public void setPrefetched (boolean _prefetched) {
    this._prefetched = _prefetched;
  }

  /**
   * @return the _isPrintable
   */
  @JsonProperty ("IsPrintable")
  public boolean isPrintable () {
    return _isPrintable;
  }

  /**
   * @param _isPrintable
   *          the _isPrintable to set
   */
  public void setPrintable (boolean _isPrintable) {
    this._isPrintable = _isPrintable;
  }

  public UUID   _scoreMark;

  public String _filePath;

  /**
   * @return the _printed
   */
  @JsonProperty ("Printed")
  public boolean isPrinted () {
    return _printed;
  }

  /**
   * @param _printed
   *          the _printed to set
   */
  public void setPrinted (boolean _printed) {
    this._printed = _printed;
  }

  /**
   * @return the _scoreMark
   */
  @JsonProperty ("ScoreMark")
  public UUID getScoreMark () {
    return _scoreMark;
  }

  /**
   * @param _scoreMark
   *          the _scoreMark to set
   */
  @Override
  public void setScoreMark (UUID _scoreMark) {
    this._scoreMark = _scoreMark;
  }

  /**
   * @return the _filePath
   */
  @Override
  @JsonProperty ("FilePath")
  public String getFilePath () {
    return _filePath;
  }

  /**
   * @param _filePath
   *          the _filePath to set
   */
  public void setFilePath (String _filePath) {
    this._filePath = _filePath;
  }

   public IITSDocument _document;
   
  //
  // public boolean equals (ItemResponse other)
  // {
  // if (ReferenceEquals (null, other))
  // return false;
  // if (ReferenceEquals (this, other))
  // return true;
  // return other.BankKey == BankKey && other.ItemKey == ItemKey;
  // }

  /**
   * @return the _document
   */
  public IITSDocument getDocument () {
    return _document;
  }

  /**
   * @param _document the _document to set
   */
  public void setDocument (IITSDocument _document) {
    this._document = _document;
  }

  public boolean equals (ItemResponse other) {
    /*
     * if (ReferenceEquals(null, other)) return false; if (ReferenceEquals(this,
     * other)) return true; return equals(other.getName(), Name);
     */
    if (other == null)
      return false;
    if (other == this)
      return true;
    return other.getBankKey () == _bankKey && other.getItemKey () == _itemKey;
  }

  @Override
  public boolean equals (Object obj) {

    if (obj == null)
      return false;
    if (obj == this)
      return true;
    if (!(obj instanceof ItemResponse))
      return false;
    return equals ((ItemResponse) obj);

  }

  // TODO
  @Override
  public int hashCode () {
    return Long.valueOf (_bankKey).hashCode () * 397 ^ Long.valueOf (_itemKey).hashCode ();
  }
}
