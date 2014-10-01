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
public abstract class ITestRequirementsInput
{
  String _displayName; ;
  String _subject;     ;
  String _testID;

  /**
   * @return the _displayName
   */
  public String getDisplayName () {
    return _displayName;
  }

  /**
   * @param _displayName
   *          the _displayName to set
   */
  public void setDisplayName (String _displayName) {
    this._displayName = _displayName;
  }

  /**
   * @return the _subject
   */
  public String getSubject () {
    return _subject;
  }

  /**
   * @param _subject
   *          the _subject to set
   */
  public void setSubject (String _subject) {
    this._subject = _subject;
  }

  /**
   * @return the _testID
   */
  public String getTestID () {
    return _testID;
  }

  /**
   * @param _testID
   *          the _testID to set
   */
  public void setTestID (String _testID) {
    this._testID = _testID;
  }

}
