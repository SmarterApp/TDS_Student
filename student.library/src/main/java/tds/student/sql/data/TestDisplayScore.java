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
public class TestDisplayScore
{
  private String _value;
  // / <summary>
  // / The reporting label
  // / </summary>
  private String _label;

  @JsonProperty ("label")
  public String getLabel () {
    return _label;
  }

  public void setLabel (String _label) {
    this._label = _label;
  }

  @JsonProperty ("value")
  public String getvalue () {
    double doubleValue;
    if (_value != null) {
      try {
        doubleValue = Double.parseDouble (_value);
        doubleValue = Math.round (doubleValue);
        return String.valueOf (doubleValue);
      } catch (NumberFormatException e) {
        //return the actual value if _value is not double value.
      }
    }

    // Return the score as-is
    return _value;
  }

  public String setvalue (String value) {
    return _value = value;
  }

  @Override
  public String toString () {
    return _label;
  }
}
