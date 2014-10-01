/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.configuration;

import AIR.Common.Configuration.AppSetting;
import AIR.Common.Configuration.AppSettings;

/**
 * @author temp_rreddy
 * 
 */
// / <summary>
// / All the settings for the test scoring engine.
// / </summary>
//TODO Milan - Uncomment class
public class TestScoringSettings
{
  
  /*public static AppSetting<bool> Enabled
  {
      get { return AppSettings.Get<bool>("testScoring.enabled", true); }
  }*/
   

  // / <summary>
  // / If this is true then tests will be lazy loaded into the test scorer
  // engine.
  // / </summary>
  // TODO
  public static AppSetting<Boolean> getLazyLoad ()
  {
//    return AppSettings.get ("testScoring.lazyLoad", false);
    return AppSettings.getBoolean ("testScoring.lazyLoad");
  }

  // / <summary>
  // / If this is true then when loading conversion tables we will pass in the
  // specific client.
  // / </summary>
  // / <remarks>
  // / At some point this should be the default and we should remove this
  // setting. Check with Paul.
  // / </remarks>
  public static AppSetting<Boolean> getLoadConversionTablesWithClient ()
  {
//    return AppSettings.get ("testScoring.loadConversionTablesWithClient", false);
    return AppSettings.getBoolean ("testScoring.loadConversionTablesWithClient");
  }

  // / <summary>
  // / If an error occurs when calling test scoring engine should we log it?
  // / </summary>
  public static AppSetting<Boolean> getLogErrors ()
  {
//    return AppSettings.get ("testScoring.logErrors", true);
    return AppSettings.getBoolean ("testScoring.logErrors");
  }

  // / <summary>
  // / Log debug information used to diagnose why a test isn't scoring.
  // / </summary>
  public static AppSetting<Boolean> getLogDebug ()
  {
//    return AppSettings.get ("testScoring.logDebug", false);
    return AppSettings.getBoolean ("testScoring.logDebug");
  }

}
