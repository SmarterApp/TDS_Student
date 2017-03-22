/*************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *************************************************************************/

package tds.student.sbacossmerge.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.juli.logging.Log;

import tds.student.sql.data.RTSAttribute;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author mskhan
 * 
 */
public class LoginInfo
{
  private tds.student.services.data.LoginInfo _loginInfo;

  // TODO: shajib
  // static double _defaultExpSecs = TimeSpan.FromMinutes(15).TotalSeconds;

  private TestSession                         _session;
  private Testee                              _testee;
  private UUID                                _token;
  private long                                _timestamp;
  private String                              _returnUrl;
  private String                              _appName;
  private String                              _clientName;
  private String                              _testeeID;
  private String                              _sessionID;
  private List<String>                        _validationErrors;
  private List<String>                        _grades;
  private GeoComponent                        _satellite;
  private List<LoginInfoAccommocation>        _globalAccs;

  @JsonProperty ("proctor")
  private Object                              _proctor = null;

  public LoginInfo (tds.student.services.data.LoginInfo loginInfo) {
    _loginInfo = loginInfo;
    _testee = loginInfo.getTestee ();
    _session = loginInfo.getSession ();
    _timestamp = createTimestamp ();
    _token = createToken ();
  }

  private UUID createToken () {
    // TODO sajib
    return UUID.randomUUID ();
  }

  private long createTimestamp () {
    // TODO Shaji/Shiva
    // TimeSpan timeSpan = DateTime.UtcNow - new DateTime (1970, 1, 1, 0, 0, 0,
    // 0, DateTimeKind.Utc);
    // Convert.ToInt64 (timeSpan.TotalSeconds);
    return Calendar.getInstance ().getTimeInMillis ();
  }

  // / <summary>
  // / Check if the login data is valid.
  // / </summary>
  // / <returns></returns>
  public boolean isValid () {
    // TODO Shajib
    // if proctor object exists and fails validation, return false
    /*
     * if (Proctor != null && !Proctor.IsValid ()) return false;
     * 
     * return Token != Guid.Empty && Token == CreateToken ();
     */
    return true;
  }

  // This is only required for deserialization purposes.
  // TODO Shiva: try it again.
  public void setValid (boolean validFlag) {
    // DO nothing.
  }

  // / <summary>
  // / Check if the login data is expired.
  // / </summary>
  public boolean isExpired () {
    long now = createTimestamp ();
    long diffSecs = now - _timestamp;
    // TODO Shajib
    return diffSecs > 15 * 60 * 1000/* _defaultExpSecs */;
  }

  public void setExpired (boolean value) {
    // TODO Shiva: are we sending expired in the .NET version.
    // If so then leave this empty.
  }

  public LoginInfo () {
    _validationErrors = new ArrayList<String> ();
  }

  public void addValidationMessage (String error) {
    _validationErrors.add (error);
  }

  public void addValidationError (String error) {
    _validationErrors.add (error);
  }

  // / <summary>
  // / The session data loaded from the db.
  // / </summary>
  @JsonProperty ("session")
  public TestSession getSession () {
    return _session;
  }

  public void setSession (TestSession _session) {
    this._session = _session;
  }

  @JsonProperty ("testee")
  public Testee getTestee () {
    return _testee;
  }

  public void setTestee (Testee _testee) {
    this._testee = _testee;
  }

  @JsonProperty ("token")
  public UUID getToken () {
    return _token;
  }

  public void setToken (UUID value) {
    this._token = value;
  }

  @JsonProperty ("timestamp")
  public long getTimestamp () {
    return _timestamp;
  }

  @JsonProperty ("returnUrl")
  public String getReturnUrl () {
    return _returnUrl;
  }

  public void setReturnUrl (String value) {
    this._returnUrl = value;
  }

  @JsonProperty ("appName")
  public String getAppName () {
    return _appName;
  }

  public void setAppName (String value) {
    this._appName = value;
  }

  @JsonProperty ("client")
  public String getClientName () {
    return _clientName;
  }

  public void setClientName (String value) {
    this._clientName = value;
  }

  @JsonProperty ("testeeID")
  public String getTesteeID () {
    return _testeeID;
  }

  public void setTesteeID (String _testeeID) {
    this._testeeID = _testeeID;
  }

  @JsonProperty ("sessionID")
  public String getSessionID () {
    return _sessionID;
  }

  public void setSessionID (String _sessionID) {
    this._sessionID = _sessionID;
  }

  @JsonProperty ("errors")
  public List<String> getValidationErrors () {
    return _validationErrors;
  }

  public void setValidationErrors (List<String> _validationErrors) {
    this._validationErrors = _validationErrors;
  }

  @JsonProperty ("grades")
  public List<String> getGrades () {
    return _grades;
  }

  public void setGrades (List<String> _grades) {
    this._grades = _grades;
  }

  @JsonProperty ("globalAccs")
  public List<LoginInfoAccommocation> getGlobalAccs () {
    return _globalAccs;
  }

  public void setGlobalAccs (List<LoginInfoAccommocation> _globalAccs) {
    this._globalAccs = _globalAccs;
  }

  @JsonProperty ("satellite")
  public GeoComponent getSatellite () {
    return _satellite;
  }

  public void setSatellite (GeoComponent _satellite) {
    this._satellite = _satellite;
  }

  public void setTimestamp (long _timestamp) {
    this._timestamp = _timestamp;
  }

  public static void main (String[] args) {
    /*
     * try{ Testee testee = new Testee (); testee.setAttributes (new
     * ArrayList<RTSAttribute>()); testee.setBirthday ("07/01/1980");
     * testee.setDistrictName ("District"); testee.setFirstName ("shiva");
     * testee.setGender ("Male"); testee.setGrade ("04"); testee.setId ("abc");
     * testee.setKey (10); testee.setSchoolName ("school name"); testee.setToken
     * (UUID.randomUUID ());
     * 
     * ObjectMapper objectMapper = new ObjectMapper(); objectMapper.writeValue
     * (System.out, testee); } catch (Exception exp) { exp.printStackTrace (); }
     */

    try {
      String loginInfoString = "{\"valid\":true,\"timestamp\":1413089926839,\"session\":{\"name\":\"TDS Session\",\"key\":\"6ff40348-bc8b-4e9d-ab9d-a919cfe8c499\",\"id\":\"GUEST Session\",\"returnStatus\":{\"reason\":\"\",\"status\":\"open\"},\"browserKey\":null,\"dateBegin\":1406316376191,\"dateEnd\":1413133125543,\"dateCreated\":1406316376176,\"isProctorless\":true,\"status\":\"Open\",\"NeedApproval\":0},\"grades\":[\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"11\"],\"token\":\"ad9af0fa-5c6d-4f27-ae96-3e8a3f58b0a0\",\"expired\":false,\"appName\":null,\"sessionID\":null,\"testee\":{\"key\":-53,\"id\":\"GUEST -53\",\"attributes\":[{\"value\":\"GUEST\",\"id\":\"FirstName\",\"label\":\"First Name\",\"SortOrder\":1,\"AtLogin\":\"REQUIRE\"},{\"value\":\"GUEST -53\",\"id\":\"ID\",\"label\":\"SSID\",\"SortOrder\":2,\"AtLogin\":\"REQUIRE\"},{\"value\":\"GUEST\",\"id\":\"LastName\",\"label\":\"Last Name\",\"SortOrder\":3,\"AtLogin\":\"VERIFY\"},{\"value\":null,\"id\":\"Grade\",\"label\":\"Grade\",\"SortOrder\":4,\"AtLogin\":\"VERIFY\"},{\"value\":null,\"id\":\"DOB\",\"label\":\"Date of Birth\",\"SortOrder\":5,\"AtLogin\":\"VERIFY\"},{\"value\":\"GUESTSchool\",\"id\":\"School\",\"label\":\"School\",\"SortOrder\":7,\"AtLogin\":\"VERIFY\"}],\"fullName\":\"GUEST, GUEST\",\"districtName\":null,\"schoolName\":\"GUESTSchool\",\"grade\":\"5\",\"token\":\"86092538-d1b6-4747-90d4-cdff5b91e83a\",\"firstName\":\"GUEST\",\"lastName\":\"GUEST\",\"birthday\":\"None\",\"gender\":null,\"isReal\":false,\"isGuest\":true},\"satellite\":{\"key\":null,\"type\":null,\"url\":\"/frontendmerge/Pages/LoginShell.xhtml\",\"dbname\":null,\"dbip\":null,\"connectionString\":null,\"dbpassword\":null,\"activeVersion\":0,\"dbuser\":null,\"cs\":null},\"returnUrl\":\"/frontendmerge/Pages/LoginShell.xhtml\",\"globalAccs\":[{\"type\":\"Color Choices\",\"codes\":[\"TDS_CC0\"]},{\"type\":\"Print Size\",\"codes\":[\"TDS_PS_L0\"]}],\"testeeID\":null,\"proctor\":null,\"client\":null,\"errors\":null}";
      LoginInfo loginInfo = (new ObjectMapper ()).readValue (loginInfoString, LoginInfo.class);
      System.err.println (loginInfo);
    } catch (Exception exp) {
      exp.printStackTrace ();
    }

  }
}
