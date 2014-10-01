/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import tds.itemrenderer.data.IITSDocument;
import tds.student.sql.data.OpportunityItem;

/**
 * @author temp_rreddy
 * 
 */
public class PageGroup extends ItemResponses
{
  private int         _number;
  private String      _groupID;
  private int         _segmentPos;
  private String      _segmentID;
  private String      _filePath;
  private int         _numRequired;
  public boolean      _printed;
  public String       _id;
  public IITSDocument _document;

  public PageGroup (OpportunityItem responseData)
  {
    _number = responseData.getPage ();
    _groupID = responseData.getGroupID ();
    _segmentPos = responseData.getSegment ();
    _segmentID = responseData.getSegmentID ();
    _filePath = responseData.getStimulusFile ();
    _numRequired = responseData.getGroupItemsRequired ();
  }

  /**
   * @return the _number
   */
  @JsonProperty ("Number")
  public int getNumber () {
    return _number;
  }

  /**
   * @return the _groupID
   */
  @JsonProperty ("ID")
  public String getGroupID () {
    return _groupID;
  }

  /**
   * @return the _segmentPos
   */
  @JsonProperty ("SegmentPosition")
  public int getSegmentPos () {
    return _segmentPos;
  }

  /**
   * @return the _segmentID
   */
  @JsonProperty ("SegmentID")
  public String getSegmentID () {
    return _segmentID;
  }

  /**
   * @return the _filePath
   */
  @JsonProperty ("FilePath")
  public String getFilePath () {
    return _filePath;
  }

  /**
   * @return the _printed
   */
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
   * @return the _id
   */
  public String getId () {
    return _groupID;
  }

  /**
   * @param _id
   *          the _id to set
   */
  public void setId (String _id) {
    this._groupID = _id;
  }

  /**
   * @return the _document
   */
  // TODO its might not require
  public IITSDocument getDocument () {
    return _document;
  }

  /**
   * @param _document
   *          the _document to set
   */
  public void setDocument (IITSDocument _document) {
    this._document = _document;
  }

  public static PageGroup Create (List<OpportunityItem> oppItems)
  {
    PageGroup pageGroup = null;

    for (OpportunityItem oppItem : oppItems)
    {
      if (pageGroup == null)
      {
        pageGroup = new PageGroup (oppItem);
      }

      ItemResponse itemResponse = new ItemResponse (oppItem);
      pageGroup.add (itemResponse);
    }

    return pageGroup;
  }

  // / <summary>
  // / First response in the group
  // / </summary>
  public ItemResponse getFirst ()
  {
	if (this.size() > 0)
		return this.get (0);
	return null;
  }

  // / <summary>
  // / Last response in the group
  // / </summary>
  public ItemResponse getLast ()
  {
	if (this.size() > 0)
		return this.get (this.size ()-1);
    return null;
  }

  // / <summary>
  // / The total number of items required to have responses.
  // / </summary>
  public int getItemsRequired ()
  {
    // adaptive algorithm will return -1 when all items are required
    int itemsRequired = _numRequired;

    if (itemsRequired == -1)
    {
      itemsRequired = this.size ();
    }
    else
    {
      // if the # of visible responses is less than the required then we need to
      // adjust the required
      itemsRequired = (this.size () < itemsRequired) ? this.size () : itemsRequired;
    }

    return itemsRequired;
  }

  // / <summary>
  // / The number of items left in the group that need responses to satisfy the
  // group or at the item level.
  // / </summary>
  public int getItemsLeftRequired ()
  {
    // total responses required to satisfy the group
    int groupRequired = getItemsRequired ();

    // total responses that are marked as being required
    int itemsRequired = 0;

    // reduce the # of required responses for each one that has been answered
    for (ItemResponse response : this)
    {
      if (response.getAnswered ())
      {
        groupRequired--;
      }
      else if (response.isRequired ())
      {
        itemsRequired++;
      }
    }

    return (groupRequired > itemsRequired) ? groupRequired : itemsRequired;
  }

  // / <summary>
  // / The number of items left in the group that are not considered to have a
  // response.
  // / </summary>
  // / <returns></returns>
  public int getItemsLeftUnanswered ()
  {
	int countUnanswered = 0;
    for (ItemResponse itemResponse : this) {
      if (!itemResponse.getAnswered ())
    	  countUnanswered++;
    }
    return countUnanswered;
  }

  // / <summary>
  // / Determines if the number of responses required for this group has been
  // met.
  // / </summary>
  @JsonProperty ("IsCompleted")
  public boolean getIsCompleted ()
  {
    return getItemsLeftRequired () == 0;
  }

  // / <summary>
  // / Are all the responses in the group valid?
  // / </summary>
  @JsonProperty ("IsValid")
  public boolean getIsValid ()
  {
    for (ItemResponse itemResponse : this) {
      if (!itemResponse.getIsValid ())
        return false;
    }
    return true;
  }

  public IITSDocument _iitsDocument;

  /**
   * @return the _iitsDocument
   */
  public IITSDocument getIitsDocument () {
    return _iitsDocument;
  }

  /**
   * @param _iitsDocument
   *          the _iitsDocument to set
   */
  public void setIitsDocument (IITSDocument _iitsDocument) {
    this._iitsDocument = _iitsDocument;
  }

  /**
   *  Make a clone of the page.
   *  
   *  @return PageGroup
   */
  public PageGroup clone () {
    OpportunityItem opportunityItem = new OpportunityItem ();
    opportunityItem.setPage (_number);
    opportunityItem.setGroupID (_groupID);
    opportunityItem.setSegment (_segmentPos);
    opportunityItem.setSegmentID (_segmentID);
    opportunityItem.setStimulusFile (_filePath);
    opportunityItem.setGroupItemsRequired (_numRequired);
    PageGroup pageGroup = new PageGroup (opportunityItem);
    pageGroup.setDocument (getIitsDocument ());
    return pageGroup;
  }

  /**
   *  Make a clone of the page.
   *  
   *  @return PageGroup
   */
  public PageGroup clone (boolean deep) {
    PageGroup pageGroup = clone();
    // add responses
    if (deep) pageGroup.addAll(this);
    return pageGroup;
  }
  
  public boolean equals (PageGroup other) {
    /*
     * if (ReferenceEquals(null, other)) return false; if (ReferenceEquals(this,
     * other)) return true; return equals(other.getName(), Name);
     */
    if (other == null)
      return false;
    if (other == this)
      return true;
    return other.getGroupID () == _groupID;
  }

  @Override
  public boolean equals (Object obj) {

    if (obj == null)
      return false;
    if (obj == this)
      return true;
    if (!(obj instanceof PageGroup))
      return false;
    return equals ((PageGroup) obj);

  }

  @Override
  public int hashCode ()
  {
    return (_groupID != null ? _groupID.hashCode () : 0);
  }
}
