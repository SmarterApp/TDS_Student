/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.data;

import java.io.Serializable;
import java.util.List;

/**
 * @author mpatel
 *
 */
public class JavascriptStackFrame implements Serializable
{

  private static final long serialVersionUID = 1L;

  /// function name, or empty for anonymous functions
  private String func;

  /// line number, or null if unknown
  private String line;

  /// JavaScript or HTML file URL
  private String url;

  /// an array of source code lines, the middle element corresponds to the correct line#
  private List<String> context;

  public String getFunc () {
    return func;
  }

  public void setFunc (String func) {
    this.func = func;
  }

  public String getLine () {
    return line;
  }

  public void setLine (String line) {
    this.line = line;
  }

  public String getUrl () {
    return url;
  }

  public void setUrl (String url) {
    this.url = url;
  }

  public List<String> getContext () {
    return context;
  }

  public void setContext (List<String> context) {
    this.context = context;
  }
  
  
  
  
}
