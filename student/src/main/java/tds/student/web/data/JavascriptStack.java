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
public class JavascriptStack implements Serializable
{
  private static final long serialVersionUID = 1L;

  /// exception message
  private String message;

  /// 'firefox', 'opera' or 'callers' -- method used to collect the stack trace
  private String mode;

  /// exception name
  private String name;

  private List<JavascriptStackFrame> stack;

  public String getMessage () {
    return message;
  }

  public void setMessage (String message) {
    this.message = message;
  }

  public String getMode () {
    return mode;
  }

  public void setMode (String mode) {
    this.mode = mode;
  }

  public String getName () {
    return name;
  }

  public void setName (String name) {
    this.name = name;
  }

  public List<JavascriptStackFrame> getStack () {
    return stack;
  }

  public void setStack (List<JavascriptStackFrame> stack) {
    this.stack = stack;
  }
  
  
  
}
