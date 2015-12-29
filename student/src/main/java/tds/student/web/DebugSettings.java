/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web;

import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.Web.BrowserOS;
import AIR.Common.Web.BrowserParser;
import AIR.Common.Web.Session.HttpContext;

/**
 * @author mpatel
 *
 */
public class DebugSettings
{
  
  public static boolean isDebuggingEnabled()
  {
        return (HttpContext.getCurrentContext () != null && 
                  HttpContext.getCurrentContext().isDebuggingEnabled ());
  }

  public static boolean ignoreForbiddenApps()
  {
        return isDebuggingEnabled () && AppSettingsHelper.getBoolean ("debug.ignoreForbiddenApps");
  }

  /// <summary>
  /// If this is true then show firebug lite on tablets.
  /// </summary>
  public static boolean showConsole()
  {
          if (!isDebuggingEnabled ()) return false;

          BrowserParser parser = BrowserParser.getCurrent ();
          
          // check if tablets
          if (parser.getOsName ().equals (BrowserOS.IOS) || 
              parser.getOsName ().equals (BrowserOS.Android))
          {
              return AppSettingsHelper.getBoolean ("debug.showConsole");
          }

          return false;
  }

  /// <summary>
  /// If this is true then include some unit testing code which helps you test items.
  /// </summary>
  /// <example>
  /// Use this bookmark to run:
  /// javascript:TestShell.UnitTests.answerPage();void(0);
  /// </example>
  public static boolean includeUnitTests()
  {
          return isDebuggingEnabled () && AppSettingsHelper.getBoolean("debug.includeUnitTests");
  }

  /*[DataMember(Name = "loadCSV")]
  public bool LoadCSV
  {
      get { return AppSettingsHelper.GetBoolean("debug.loadCSV"); }
      set {}
  }*/
  
  public static boolean ignoreBrowserChecks()
  {
         return isDebuggingEnabled () && AppSettingsHelper.getBoolean ("debug.ignoreBrowserChecks");
  }
}
