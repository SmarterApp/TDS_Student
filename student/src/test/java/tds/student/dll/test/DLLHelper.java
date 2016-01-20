/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.dll.test;

import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;

import AIR.Common.DB.AbstractDAO;

public class DLLHelper  extends AbstractDAO {
	 
	  public String fixDataBasenames(String query, Map<String, String> paramMap) {
		    StrSubstitutor substitutor = new StrSubstitutor (paramMap);
		    return substitutor.replace (query);
	  }

}
