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
import java.util.Date;

import java.util.UUID;

import org.apache.commons.lang.NotImplementedException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import TDS.Shared.Data.ReturnStatus;

import AIR.Common.Helpers.Constants;

public class TestSelection extends ITestRequirementsInput
{
  private String           _testKey;
  private String           _testID;
  private String           _mode;
  private int              _opportunity;
  private int              _maxOpportunities;
  private String           _displayName;
  private int              _sortOrder;
  private Status           _testStatus;
  private TestRequirements _requirements;
  private int              _testLength;
  private UUID             _oppKey = Constants.UUIDEmpty;
  private Date             _dateCompleted;
  private String           _subject;
  private String           _grade;
  private String		   _reasonText;
  private String		   _reasonKey;
  private String		   _warningText;
  private String		   _warningKey;
  
  // TODO
  // @JsonProperty("requirements")
  public TestSelection () {
    this._requirements = new TestRequirements ();
  }

  @JsonProperty ("key")
  public String getTestKey () {
    return _testKey;
  }

  public void setTestKey (String _testKey) {
    this._testKey = _testKey;
  }

  // TODO shiva: the parent class has these two properties. Why do we need them
  // here?
  @JsonProperty ("id")
  @Override
  public String getTestID () {
    return _testID;
  }

  // TODO shiva: the parent class has these two properties. Why do we need them
  // here?
  @Override
  public void setTestID (String _testID) {
    this._testID = _testID;
    super.setTestID (_testID);
  }

  @JsonProperty ("mode")
  public String getMode () {
    return _mode;
  }

  public void setMode (String _mode) {
    this._mode = _mode;
  }

  @JsonProperty ("opportunity")
  public int getOpportunity () {
    return _opportunity;
  }

  public void setOpportunity (int _opportunity) {
    this._opportunity = _opportunity;
  }

  @JsonProperty ("maxOpportunities")
  public int getMaxOpportunities () {
    return _maxOpportunities;
  }

  public void setMaxOpportunities (int _maxOpportunities) {
    this._maxOpportunities = _maxOpportunities;
  }

  @JsonProperty ("displayName")
  public String getDisplayName () {
    return _displayName;
  }

  public void setDisplayName (String _displayName) {
    this._displayName = _displayName;
  }

  @JsonProperty ("sortOrder")
  public int getSortOrder () {
    return _sortOrder;
  }

  public void setSortOrder (int _sortOrder) {
    this._sortOrder = _sortOrder;
  }


  /** A reason why you cannot take the test. 
   * This is text that most likely comes from the database.
   */
  @JsonProperty ("reasonText")
  public String getReasonText () { return _reasonText; }
  
  public void setReasonText (String text) {
	  this._reasonText = text;
  }

  /** A reason why you cannot take the test. 
   * This is a message key and will need to be translated on the client.
   * This takes precedence over the Reason property if it is not null.
   */
  @JsonProperty ("reasonKey")
  public String getReasonKey () { return _reasonKey; }
  
  public void setReasonKey (String key) {
	  this._reasonKey = key;
  }
  
  /** A warning when you click on the test.
   */
  @JsonProperty ("warningText")
  public String getWarningText () { return _warningText; }
  
  public void setWarningText (String text) {
	  this._warningText = text;
  }

  /** A warning when you click on the test.
   * This is a message key and will need to be translated on the client.
   * This takes precedence over the Warn property if it is not null.
   */
  @JsonProperty ("warningKey")
  public String getWarningKey () { return _warningKey; }
  
  public void setWarningKey (String key) {
	  this._warningKey = key;
  }
  
  @JsonProperty ("status")
  public int getNumericStatus()
  {
    //Shiva: no null check as we are relying on ReturnStatus below to fill in the right one.
    return getTestStatus ().ordinal ();
  }
  
  @JsonIgnore
  public Status getTestStatus () {
    return _testStatus;
  }

  public void setTestStatus (Status testStatus) {
    this._testStatus = testStatus;
  }

  public TestRequirements getRequirements () {
    return _requirements;
  }

  public void setRequirements (TestRequirements _requirements) {
    this._requirements = _requirements;
  }

  public int getTestLength () {
    return _testLength;
  }

  public void setTestLength (int _testLength) {
    this._testLength = _testLength;
  }

  public UUID getOppKey () {
    return _oppKey;
  }

  public void setOppKey (UUID _oppKey) {
    this._oppKey = _oppKey;
  }

  public Date getDateCompleted () {
    return _dateCompleted;
  }

  public void setDateCompleted (Date _dateCompleted) {
    this._dateCompleted = _dateCompleted;
  }

  public String getSubject () {
    return _subject;
  }

  public void setSubject (String _subject) {
    this._subject = _subject;
  }

  public String getGrade () {
    return _grade;
  }

  public void setGrade (String _grade) {
    this._grade = _grade;
  }

  public void SetReturnStatus (ReturnStatus returnStatus) {
    _reasonText = returnStatus.getReason();
    OpportunityStatusType oppStatusType = OpportunityStatusExtensions.parse (returnStatus.getStatus ());

    // map the opp status to test selection status
    // Note: for 2011 Denied = Disabled and it seems 2012 Disabled =
    // Disabled
    switch (oppStatusType) {
    case Pending:
      _testStatus = Status.Start;
      break;
    case Suspended:
      _testStatus = Status.Resume;
      break;
    case NotApplicable:
      _testStatus = Status.Hidden;
      break;
    case Denied:
      _testStatus = Status.Disabled;
      break;
    case Disabled:
      _testStatus = Status.Disabled;
      break;
    default:
      throw new NotImplementedException (String.format ("Status type %1$s not supported yet.", oppStatusType.toString ())); // TODO
    }
  }

  @Override
  public String toString () {
    return this._testKey;
  }

  public enum Status {
    Disabled, // show test with reason
    Hidden, // hide test from student
    Start, // test has never been started
    Resume // test was previously started
  }
}
