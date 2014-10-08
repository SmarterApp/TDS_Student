/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.air.test;

/**
 * @author temp_rreddy
 * 
 */
public class TestInitializationException extends RuntimeException
{
  public TestInitializationException (String message) {
    super (message);
  }

  public TestInitializationException (String message, Exception ex) {
    super (message, ex);
  }

  public TestInitializationException (Exception exp) {
    super (exp);
  }
}
