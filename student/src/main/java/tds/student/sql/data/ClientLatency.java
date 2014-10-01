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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import AIR.Common.Utilities.TDSStringUtils;

/**
 * @author temp_rreddy
 * 
 */
public class ClientLatency
{
  private int  _itemPage;
  private int  _numItems;
  private int  _requestTime;
  private int  _loadAttempts;
  private Date _loadDate;
  private int  _loadTime;
  private Date _createDate;
  private int  _visitCount;
  private int  _visitTime;
  private Date _visitDate;

  /**
   * @return
   */
  @JsonProperty ("itemPage")
  public int getItemPage () {
    return _itemPage;

  }

  public void setItemPage (int value) {
    setRequiredValue ("itemPage");
    _itemPage = value;

  }

  @JsonProperty ("numItems")
  public int getNumItems () {
    return _numItems;
  }

  public void setNumItems (int value) {
    setRequiredValue ("numItems");
    _numItems = value;
  }

  @JsonProperty ("requestTime")
  public int getRequestTime () {
    return _requestTime;
  }

  public void setRequestTime (int value) {
    setRequiredValue ("requestTime");
    _requestTime = value;
  }

  @JsonProperty ("loadAttempts")
  public int getLoadAttempts () {
    return _loadAttempts;
  }

  public void setLoadAttempts (int value) {
    setRequiredValue ("loadAttempts");
    _loadAttempts = value;
  }

  @JsonProperty ("loadDate")
  public Date getLoadDate () {
    return fixSqlDateTimeNullable (_loadDate);
  }

  @JsonIgnore
  public void setLoadDate (Date value) {
    setRequiredValue ("loadDate");
    _loadDate = value;
  }
  
  @JsonProperty
  public void setLoadDate (String value) {

    setRequiredValue ("loadDate");
    _loadDate = parseDate(value);
 
  }
  private Date parseDate(String t) {

    if (t == null)
      return null;
    if (t.startsWith ("/Date(")) {
      t = t.substring (6, t.length ()- 2);
    }
    return new Date(Long.parseLong (t));
  }
  @JsonProperty ("loadTime")
  public int getLoadTime () {
    return _loadTime;
  }

  public void setLoadTime (int value) {
    setRequiredValue ("loadTime");
    _loadTime = value;
  }

  @JsonProperty ("createDate")
  public Date getCreateDate () {
    return fixSqlDateTimeNullable (_createDate);
    //return null;
  }

  @JsonIgnore
  public void setCreateDate (Date value) {
    setRequiredValue ("createDate");
    _createDate = value;
  }

  @JsonProperty
  public void setCreateDate (String value) {

    setRequiredValue ("createDate");
    _createDate = parseDate(value);
 
  }
  @JsonProperty ("visitCount")
  public int getVisitCount () {
    return _visitCount;
  }

  public void setVisitCount (int value) {
    setRequiredValue ("visitCount");
    _visitCount = value;
  }

  @JsonProperty ("visitTime")
  public int getVisitTime () {
    return _visitTime;
  }

  public void setVisitTime (int value) {
    setRequiredValue ("visitTime");
    _visitTime = value;
  }

  @JsonProperty ("visitDate")
  public Date getVisitDate () {
    return fixSqlDateTimeNullable (_visitDate);
  }

  @JsonIgnore
  public void setVisitDate (Date value) {
    setRequiredValue ("visitDate");
    _visitDate = value;
  }

  @JsonProperty
  public void setVisitDate (String value) {
    setRequiredValue ("visitDate");
    _visitDate = parseDate(value);
  }
  @JsonProperty ("toolsUsed")
  public String                    _toolsUsed;

  private HashMap<String, Boolean> _requiredValues;

  public String getToolsUsed () {
    return _toolsUsed;
  }

  public void setToolsUsed (String _toolsUsed) {
    this._toolsUsed = _toolsUsed;
  }

  private void onDeserializing () {
    // TODO
    _requiredValues = new HashMap<String, Boolean> ();
    _requiredValues.put ("itemPage", false);
    _requiredValues.put ("numItems", false);
    _requiredValues.put ("requestTime", false);
    _requiredValues.put ("loadAttempts", false);
    _requiredValues.put ("loadDate", false);
    _requiredValues.put ("loadTime", false);
    _requiredValues.put ("createDate", false);
    _requiredValues.put ("visitCount", false);
    _requiredValues.put ("visitTime", false);
    _requiredValues.put ("visitDate", false);

    /*
     * { {"itemPage", false}, {"numItems", false}, {"requestTime", false},
     * {"loadAttempts", false}, {"loadDate", false}, {"loadTime", false},
     * {"createDate", false}, {"visitCount", false}, {"visitTime", false},
     * {"visitDate", true} };
     */
  }

  private void setRequiredValue (String name) {
   // if (_requiredValues != null && _requiredValues.containsKey (name)) {
      // TODO
    if(_requiredValues == null)
    _requiredValues = new HashMap<String, Boolean> ();
      _requiredValues.put (name, true);
  //  }
  }

  private boolean hasRequiredValue (String name) {
    return (_requiredValues.containsKey (name) && _requiredValues.get (name));
  }

  private static Date fixSqlDateTimeNullable (Date dateTime) {
    if (dateTime == null)
      return dateTime;
  //  return fixSqlDateTime (dateTime);
    return dateTime;
  }

//  private static Date fixSqlDateTime (Date dateTime) {
//    // TODO
//    if (dateTime < SqlDateTime.MinValue.Value) {
//      return SqlDateTime.MinValue.Value;
//    }
//
//    if (dateTime > SqlDateTime.MaxValue.Value) {
//      return SqlDateTime.MaxValue.Value;
//    }
//
//    return dateTime;
//  }

  public List<String> getErrors () {
    List<String> errors = new ArrayList<String> ();

    for (Entry<String, Boolean> e : _requiredValues.entrySet ()) {
      String key = e.getKey ();
      Boolean value = e.getValue ();
      if (!value) {
        errors.add (String.format ("Value does not exist: %1$s", key));
      }
      // TODO
      /*
       * for (HashMap<String, Boolean> requiredValue : _requiredValues) { if
       * (!requiredValue.Value) { errors.Add (String.Format
       * ("Value does not exist: {0}", requiredValue.Key)); } }
       */

      if (hasRequiredValue ("createDate") && _createDate != getCreateDate ()) {
        errors.add (String.format ("Min/max out of range: createDate - %1$s", _createDate));
      }

      if (hasRequiredValue ("loadDate") && _loadDate != getLoadDate ()) {
        errors.add (String.format ("Min/max out of range: loadDate - %1$s", _loadDate));
      }

    }
    return errors;
  }
}
