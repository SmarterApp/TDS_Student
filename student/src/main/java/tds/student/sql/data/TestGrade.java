/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
public class TestGrade
{
  private String _text = null;

  @JsonProperty ("Text")
  public String getText () {
    return _text;
  }

  // TODO
  public int getInteger () {
    int gradeInt;
    try {
    gradeInt = Integer.parseInt (_text);
    } catch (NumberFormatException e) {
      gradeInt = 0;
    }
    return gradeInt;
  }

  public TestGrade (String grade) {
    _text = grade.trim ();
  }

  @Override
  public String toString () {
    return _text;
  }

  public boolean equals (TestGrade other) {
    if (other == null)
      return false;
    if (other == this)
      return true;
    return other.getText ().equalsIgnoreCase (_text);

  }

  @Override
  public boolean equals (Object obj) {
    // TODO
    // if (ReferenceEquals(null, obj)) return false;
    // if (ReferenceEquals(this, obj)) return true;
    if (obj == null)
      return false;
    if (obj == this)
      return true;
    if (!(obj instanceof TestGrade))
      return false;
    return equals ((TestGrade) obj);

  }

  @Override
  public int hashCode () {
    return (_text != null ? _text.hashCode () : 0);
  }
}
