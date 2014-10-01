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
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestRequirements
{
  private double  _flashVersion;
  private boolean _hasAudio;
  private boolean _hasRecorder;

  public TestRequirements () {
  }

  @JsonProperty ("flashVersion")
  public double getFlashVersion () {
    return _flashVersion;
  }

  public void setFlashVersion (double _flashVersion) {
    this._flashVersion = _flashVersion;
  }

  @JsonProperty ("hasAudio")
  public boolean isHasAudio () {
    return _hasAudio;
  }

  public void setHasAudio (boolean _hasAudio) {
    this._hasAudio = _hasAudio;
  }

  @JsonProperty ("hasRecorder")
  public boolean isHasRecorder () {
    return _hasRecorder;
  }

  public void setHasRecorder (boolean _hasRecorder) {
    this._hasRecorder = _hasRecorder;
  }

  public void lookup (String clientName, ITestRequirementsInput input) {
    String testID = input.getTestID (); // e.x., Oregon-ELPA Speaking-9-12
    String testName = input.getDisplayName ().toUpperCase (); // e.x., High
    // School
    // ELPA
    // Speaking, Grades
    // 3 -
    // 12 New Item Types
    String subject = input.getSubject ().toUpperCase ();

    // Oregon OP grid subjects: math, reading, science
    if ("OREGON".equals (clientName) && ("MATHEMATICS".equals (subject) || "READING".equals (subject) || "SCIENCE".equals (subject))) {
      this._flashVersion = 9.0;
    }

    // Oregon rules
    if (testID.contains ("ELPA")) {
      this._flashVersion = 9.0;
      this._hasAudio = true;
      this._hasRecorder = true;
    }

    // Oregon PT grid
    if ("OREGON_PT".equalsIgnoreCase (clientName) && testName.contains ("NEW ITEM TYPES")) {
      this._flashVersion = 9.0;
    }

    // Delaware/Hawaii rules
    if (clientName.contains ("DELAWARE") || clientName.contains ("HAWAII")) {
      this._flashVersion = 9.0;
    }

    // BUG 94663: Disabled any Flash detection
    this._flashVersion = 0.0;
  }
}
