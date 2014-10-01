/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import AIR.Common.Configuration.ConfigurationSection;

/**
 * @author temp_rreddy
 * 
 */
// / <summary>
// / All the settings for the item scoring engine and timer.
// / </summary>
// TODO please check all the methods, whether its correct or not?
@Component
public class ItemScoringSettings
{
  // / <summary>
  // / If this is true then item scoring is enabled. Otherwise nothing gets
  // scored.
  // / </summary>
  @Autowired
  private ConfigurationSection appSettings;
  
  public  Boolean getEnabled ()
  {
    return appSettings.getBoolean  ("itemScoring.enabled", true);
  }

  // / <summary>
  // / If this is true then we will log all exceptions that occur when item
  // scoring.
  // / </summary>
  public  Boolean getDebug ()
  {
    return appSettings.getBoolean ("itemScoring.debug", false);
  }

  // / <summary>
  // / This is the url used to perform item scoring.
  // / </summary>
  public  String getServerUrl ()
  {
    return appSettings.get ("itemScoring.serverUrl", null);
  }

  // / <summary>
  // / This is the url for
  // / </summary>
  public  String getCallbackUrl ()
  {
    return appSettings.get  ("itemScoring.callbackUrl", null);
  }

  public  Boolean getAlwaysLoadRubric ()
  {
    return appSettings.getBoolean ("itemScoring.alwaysLoadRubric", false);
  }

  public  Boolean getTimerEnabled ()
  {
    return appSettings.getBoolean ("itemScoring.timer.enabled", true);
  }

  public  Integer getTimerInterval ()
  {
    return appSettings.getInt32 ("itemScoring.timer.interval", 5);
  }

  public  Integer getTimerPendingMins ()
  {
    return appSettings.getInt32 ("itemScoring.timer.pendingMins", 15);
  }

  public  Integer  getTimerMaxAttempts ()
  {
    return appSettings.getInt32 ("itemScoring.timer.maxAttempts", 10);
  }
}
