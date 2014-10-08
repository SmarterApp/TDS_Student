/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReadOnlyException;

/**
 * @author temp_rreddy
 * 
 */
public class AccommodationsTest extends AbstractTest
{
  Accommodations accommodations = new Accommodations ();
  private static final Logger _logger = LoggerFactory.getLogger(AccommodationsTest.class);
  
  @Test
  public void testGetDefaults () {
    List<AccommodationValue> accommodationValueList = accommodations.getDefaults ();
    _logger.info ("AccommodationValueList Size:" + accommodationValueList);
  }

  @Test
  public void testcreateType () throws ReadOnlyException {
    try {
      AccommodationType accommodationType = accommodations.createType ("TEST", true, true, true, true, "TEST1", false);
      _logger.info ("accommodationType :" + accommodationType);
      assertTrue (accommodationType != null);
    } catch (ReadOnlyException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
      _logger.error("accommodationType :" + e.getMessage ());
      throw e;
    }
  }

  @Test
  public void testGetValues () {
    try {
     /*  AccommodationType accommodationType = accommodations.createType
       ("TEST", true, true, true, true, "TEST1", false);
       Accommodations accommodations = new Accommodations();
       List<String> valuesList = new ArrayList<String> ();
       Predicate predicate = null;
       accommodations.replaceWith (accommodations, predicate); */ 

      accommodations.unionWith (accommodations);
    } catch (ReadOnlyException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
      _logger.error("accommodationType :" + e.getMessage ());
    }
  }

}
