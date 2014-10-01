/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.data;

import java.util.HashMap;

/**
 * @author temp_rreddy
 * 
 */
public class LoginKeyValues extends HashMap<String, String>
{
  String keyandvalue = "";

  @Override
  public String toString ()
  {
    for (java.util.Map.Entry<String, String> e : super.entrySet ()) {
      String key = e.getKey ();
      if(key!=null){
        Object value = e.getValue ();
        keyandvalue = keyandvalue + key + ":" + value +";";
      }
    }
    if(keyandvalue!=null && keyandvalue.length () > 0) {
      keyandvalue = keyandvalue.substring (0,keyandvalue.length ()-1);
    }
    return keyandvalue;

  }
}
