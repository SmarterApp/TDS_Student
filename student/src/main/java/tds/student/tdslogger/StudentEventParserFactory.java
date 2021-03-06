/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.student.tdslogger;

import org.opentestsystem.delivery.logging.EventParser;
import org.opentestsystem.delivery.logging.EventParserFactory;

import java.util.HashMap;
import java.util.Map;


public class StudentEventParserFactory extends EventParserFactory {

  private static Map<String, Class<? extends EventParser>> classMap = new HashMap<>();

  static {
    // Currently there are no custom event parsers installed. They would be mapped here.
  }

  @Override
  protected Map<String, Class<? extends EventParser>> getEventParserClassMap() {
    return classMap;
  }
}
