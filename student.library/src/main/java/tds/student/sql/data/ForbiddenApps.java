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
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import AIR.Common.Web.BrowserOS;

/**
 * @author temp_rreddy
 * 
 */
public class ForbiddenApps
{
  public class Process
  {
    // / <summary>
    // / The OS of the application.
    // / </summary>
    private String _os;

    // / <summary>
    // / Process name of the application.
    // / </summary>

    private String _name;

    // / <summary>
    // / Process description of the application.
    // / </summary>

    private String _description;

    @JsonProperty ("OS")
    public String getOs () {
      return _os;
    }

    public void setOs (String _os) {
      this._os = _os;
    }

    @JsonProperty ("name")
    public String getName () {
      return _name;
    }

    public void setName (String _name) {
      this._name = _name;
    }

    @JsonProperty ("desc")
    public String getDescription () {
      return _description;
    }

    public void setDescription (String _description) {
      this._description = _description;
    }

    public String toString () {
      return _name;
    }
  }

  public class ExcludedSchool
  {
    private String _districtName;
    private String _districtID;
    private String _schoolName;
    private String _schoolID;

    @JsonProperty ("DistrictName")
    public String getDistrictName () {
      return _districtName;
    }

    public void setDistrictName (String _districtName) {
      this._districtName = _districtName;
    }

    @JsonProperty ("DistrictID")
    public String getDistrictID () {
      return _districtID;
    }

    public void setDistrictID (String _districtID) {
      this._districtID = _districtID;
    }

    @JsonProperty ("SchoolName")
    public String getSchoolName () {
      return _schoolName;
    }

    public void setSchoolName (String _schoolName) {
      this._schoolName = _schoolName;
    }

    @JsonProperty ("SchoolID")
    public String getSchoolID () {
      return _schoolID;
    }

    public void setSchoolID (String _schoolID) {
      this._schoolID = _schoolID;
    }

    public String toString () {
      return _schoolID;
    }
  }

  // useragent/OS ID
  private final HashMap<String, String>             _clientOS        = new HashMap<String, String> ();
  private final HashMap<String, ArrayList<Process>> _applications    = new HashMap<String, ArrayList<Process>> ();
  private final List<ExcludedSchool>                _excludedSchools = new ArrayList<ExcludedSchool> ();

  public void addAgentOS (String agentOS, String osID) {
    if (!_clientOS.containsKey (agentOS)) {
      _clientOS.put (agentOS, osID);
    }
  }

  public void addApplication (String osID, Process process) {
    if (!_applications.containsKey (osID)) {
       ArrayList<Process> processList = new ArrayList<Process>() ; 
       processList.add (process);
      _applications.put (osID, processList);
    } else  { // _applications contains key == osID
       _applications.get(osID).add(process); 
    }
  }

  public void addExcludedSchool (ExcludedSchool excludedSchool) {
    _excludedSchools.add (excludedSchool);
  }

  public List<Process> getApps (BrowserOS platform) {
    String osID = platform.toString ();

    List<Process> osApps;
    osApps = _applications.get (osID);

    return osApps;
  }

  public List<ExcludedSchool> getExcludedSchools () {
    return _excludedSchools;
  }

  public boolean isSchoolExcluded (String schoolID) {

    for (ExcludedSchool _excludedSchool : _excludedSchools) {
      if (schoolID.toLowerCase ().equals (_excludedSchool.getSchoolID ().toLowerCase ())) {
        return true;
      }
    }

    return false;
  }
}
