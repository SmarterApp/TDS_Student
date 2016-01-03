/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data.dummy;

import java.util.HashSet;
import java.util.Map;

import tds.itemrenderer.data.AccLookup;
import tds.student.sql.data.Accommodations;

public class AccommodationsExtensions
{
  public static Accommodations createAccommodations (AccLookup accLookup) {
    Accommodations accommodations = new Accommodations (accLookup.getPosition (), accLookup.getId (), null);

    for (Map.Entry<String, HashSet<String>> keyValuePair : accLookup.getTypes ().entrySet ()) {
      String type = keyValuePair.getKey ();

      for (String code : keyValuePair.getValue ()) {
        // make sure you allow combining (bug #57740)
        accommodations.create (type, code, null, true, true, true, true, null, false, false, true, false);
      }
    }

    return accommodations;

  }
}
