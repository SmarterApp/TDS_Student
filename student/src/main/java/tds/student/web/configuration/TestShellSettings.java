/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.configuration;

import AIR.Common.Configuration.AppSetting;
import AIR.Common.Configuration.AppSettings;
import AIR.Common.Web.UrlHelper;

public class TestShellSettings
{
  // / <summary>
  // / How many seconds do we check to auto save items.
  // / </summary>
  // / <remarks>
  // / If this is 0 then the timer is disabled.
  // / </remarks>
  public static AppSetting<Integer> getAutoSaveInterval ()
  {
    return AppSettings.getInteger ("tds.testshell.autoSaveInterval", 120);
  }

  // / <summary>
  // / How many seconds before we close the timeout dialog and log the user out.
  // / </summary>
  public static AppSetting<Integer> getTimeoutDialog ()
  {
    return AppSettings.getInteger ("tds.testshell.timeoutDialog", 30);
  }

  // / <summary>
  // / How many seconds in between polling if the student is running any
  // forbidden apps.
  // / </summary>
  // / <remarks>
  // / If this is 0 then the timer is disabled.
  // / </remarks>
  public static AppSetting<Integer> getForbiddenAppsInterval ()
  {
    return AppSettings.getInteger ("tds.testshell.forbiddenAppsInterval", 60);
  }

  // / <summary>
  // / If this is false then we save the current page when the user is inactive
  // when pausing.
  // / </summary>
  public static AppSetting<Boolean> isDisableSaveWhenInactive ()
  {
    return AppSettings.getBoolean ("tds.testshell.disableSaveWhenInactive");
  }

  // / <summary>
  // / If this is false then we save the current page when a forbidden app is
  // detected when pausing.
  // / </summary>
  public static AppSetting<Boolean> isDisableSaveWhenForbiddenApps ()
  {
    return AppSettings.getBoolean ("tds.testshell.disableSaveWhenForbiddenApps");
  }

  // / <summary>
  // / If this is true then you can leave a page when audio is playing and it
  // will stop.
  // / </summary>
  public static AppSetting<Boolean> isAllowSkipAudio ()
  {
    return AppSettings.getBoolean ("tds.testshell.allowSkipAudio");
  }

  // / <summary>
  // / If this is true then show the segment label group in the page dropdown.
  // / </summary>
  public static AppSetting<Boolean> isShowSegmentLabels ()
  {
    return AppSettings.getBoolean ("tds.testshell.showSegmentLabels");
  }

  // / <summary>
  // / How many seconds until the audio timer will fire if the audio init event
  // doesn't fire.
  // / </summary>
  public static AppSetting<Integer> getAudioTimeout ()
  {
    return AppSettings.getInteger ("tds.testshell.audioTimeout", 180);
  }

  public static AppSetting<Boolean> isEnableLogging ()
  {
    return AppSettings.getBoolean ("tds.testshell.enableLogging");
  }

  // TODO needs review
  public static AppSetting<String> getDictionaryUrl ()
  {
    // .net code
    /*
     * string defaultUrl = UrlHelper.GetApplication() +
     * "Pages/Dictionary/default.aspx"; return
     * AppSettings.Get<string>("tds.testshell.dictionaryUrl", defaultUrl);
     */
    return AppSettings.getString ("tds.testshell.dictionaryUrl");
  }
}
