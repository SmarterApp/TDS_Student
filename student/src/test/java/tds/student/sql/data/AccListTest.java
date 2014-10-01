/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.test.framework.AbstractTest;

/**
 * @author temp_rreddy
 * 
 */
public class AccListTest extends AbstractTest
{
  private static Logger _logger = LoggerFactory.getLogger(AccListTest.class);
  AppExterns appExterns = new AppExterns ();

  @Test
  public void test () throws ParseException {

    SimpleDateFormat df = new SimpleDateFormat ("MM/dd/yyyy");
    Date sourceDateTime;
    try {
      sourceDateTime = df.parse ("4/06/2011");
      Date date = appExterns.convertEST_XST (sourceDateTime);
      _logger.info ("DATE VALUE" + date);      
    } catch (ParseException e) {
       _logger.error (e.getMessage (),e);
       throw e;
    }

  }

}
