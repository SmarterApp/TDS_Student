/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

/**
 * @author temp_rreddy
 * 
 */
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import AIR.Common.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Testee
{

  private String           _id;
  private long             _key;
  private String           _firstName;
  private String           _lastName;
  private String           _grade;
  private String           _birthday;
  private String           _gender;
  private String           _districtName;
  private String           _schoolName;
  private boolean          _isReal;
  private UUID             _token = UUID.randomUUID ();
  private boolean          _isGuest;
  private TesteeAttributes _attributes;

  // token, isGuest, testeeAttr,

  public Testee () {
  }

  @JsonProperty ("id")
  public String getId () {
    if (_isGuest) {
      return "GUEST";
    }

    return _id;
  }

  public void setId (String _id) {
    this._id = _id;
  }

  @JsonProperty ("key")
  public long getKey () {
    return _key;
  }

  public void setKey (long _key) {
    this._key = _key;
  }

  @JsonProperty ("firstName")
  public String getFirstName () {
    return _firstName;
  }

  public void setFirstName (String _firstName) {
    this._firstName = _firstName;
  }

  @JsonProperty ("lastName")
  public String getLastName () {
    return _lastName;
  }

  public void setLastName (String _lastName) {
    this._lastName = _lastName;
  }

  @JsonProperty ("grade")
  public String getGrade () {
    return _grade;
  }

  public void setGrade (String _grade) {
    this._grade = _grade;
  }

  @JsonProperty ("birthday")
  public String getBirthday () {
    return _birthday;
  }

  public void setBirthday (String _birthday) {
    this._birthday = _birthday;
  }

  @JsonProperty ("gender")
  public String getGender () {
    return _gender;
  }

  public void setGender (String _gender) {
    this._gender = _gender;
  }

  @JsonProperty ("districtName")
  public String getDistrictName () {
    return _districtName;
  }

  public void setDistrictName (String _districtName) {
    this._districtName = _districtName;
  }

  @JsonProperty ("schoolName")
  public String getSchoolName () {
    return _schoolName;
  }

  public void setSchoolName (String _schoolName) {
    this._schoolName = _schoolName;
  }

  @JsonProperty ("isReal")
  public boolean isReal () {
    return _isReal;
  }

  public void setReal (boolean _isReal) {
    this._isReal = _isReal;
  }

  public String getFullName () {
    if (StringUtils.isEmpty (_lastName) && StringUtils.isEmpty (_firstName))
      return "";
    return _lastName + ", " + _firstName;
  }

  public void setFullName(String value)
  {
    //Shiva: only for deserialization reasons. It is required in the serialization but not in deserialization and hence
    //I am not going to mark it as JsonIgnore.
  }
  
  @JsonProperty ("attributes")
  public TesteeAttributes getAttributes () {
    return _attributes;
  }

  public void setAttributes (List<RTSAttribute> value) {
    if (value != null) {
      _attributes = new TesteeAttributes ();
      _attributes.addAll (value);
    }
  }

  public Testee (TesteeAttributes testeeAttributes) {
    _attributes = testeeAttributes;
    _key = testeeAttributes.getKey ();
    _id = testeeAttributes.getValue ("ID");
    _firstName = testeeAttributes.getValue ("FirstName");
    _lastName = testeeAttributes.getValue ("LastName");
    _grade = testeeAttributes.getValue ("Grade");
    _schoolName = testeeAttributes.getValue ("School");
    _districtName = testeeAttributes.getValue ("District");

    // get date and convert into presentable string
    Date birthdayDT = testeeAttributes.getDateTime ("DOB");

    if (birthdayDT != null && birthdayDT.after (DateTime.getMinValue ())) {
      // _birthday = birthdayDT.toString ("MMMM d, yyyy");
      // TODO Ravi/Shiva see if you can use AbstractDateUtilDll.dateFormat here
      // instead.
      SimpleDateFormat sdf = new SimpleDateFormat ("MMMM d, yyyy");
      _birthday = sdf.format (birthdayDT);
    } else {
      _birthday = "None";
    }

  }

  @JsonProperty ("token")
  public UUID getToken () {
    return _token;
  }

  public void setToken (UUID value) {
    this._token = value;
  }

  @JsonProperty ("isGuest")
  public boolean isGuest () {
    _isGuest = getKey () <= 0;
    return _isGuest;
  }
  
  public void setIsGuest(boolean value)
  {
    //DO nothing.
  }
  
  //The "name" attribute is only required because Javascript seems to be sending that. 
  @JsonProperty("name")
  public String getName()
  {
    return getFullName ();
  }

  public void setName(String value)
  {
    //TODO Shiva: should we be saving this instead?
  }

}
