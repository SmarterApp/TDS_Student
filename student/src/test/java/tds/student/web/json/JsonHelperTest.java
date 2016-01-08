/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.Json.JsonHelper;
import AIR.test.framework.AbstractTest;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class JsonHelperTest extends AbstractTest
{
  private static final Logger _logger = LoggerFactory.getLogger (JsonHelperTest.class);

  @Test
  public void testString () throws JsonGenerationException, JsonMappingException, IOException {
    List<String> styles = new ArrayList<String> ();
    styles.addAll (Arrays.asList ("one", "two", "three"));
    String str;
    str = JsonHelper.serialize (styles);
    List<String> styles2 = new ArrayList<String> ();
    styles2 = JsonHelper.deserialize (str, styles2.getClass ());
    Assert.assertTrue (styles2.equals (styles));
  }

  @Test
  public void testInt () throws JsonGenerationException, JsonMappingException, IOException {
    List<Integer> styles = new ArrayList<Integer> ();
    styles.addAll (Arrays.asList (1, 2, 3));
    String str;
    str = JsonHelper.serialize (styles);
    List<Integer> styles2 = new ArrayList<Integer> ();
    styles2 = JsonHelper.deserialize (str, styles2.getClass ());
    Assert.assertTrue (styles2.equals (styles));
  }

}
