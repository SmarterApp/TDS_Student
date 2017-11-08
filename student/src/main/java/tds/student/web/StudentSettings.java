/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.student.sql.abstractions.IConfigRepository;
import tds.student.sql.data.AppExterns;
import tds.student.sql.data.PTSetup;
import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.Web.CookieHelper;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.RuntimeReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */

@Component
@Scope ("request")
public class StudentSettings /* extends TDSSettings */
{

  private static final Logger _logger = LoggerFactory.getLogger (StudentSettings.class);
  @Autowired
  private IConfigRepository   _configRepository;

  @Autowired
  private TDSSettings         _tdsSettings;

  public boolean isSecureBrowserRequired() throws ReturnStatusException {
    return _configRepository.isSecureBrowserRequired();
  }

  public String getClientStylePath () throws RuntimeReturnStatusException {
    // TODO:
    // For debugging, use AppSettings.Get to override testeecheckin address.
    AppExterns appExterns = null;
    try {
      appExterns = _configRepository.getExterns ();
    } catch (ReturnStatusException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
      _logger.debug (String.format ("Error getting client style path. %s.", e.getMessage ()), e);
      throw new RuntimeReturnStatusException (e);
    }
    return appExterns.getClientStylePath ();
  }

  // / <summary>
  // / Get the mode name for the application.
  // / </summary>
  public String getModeName () {
    // check cookie for mode name
    String modeName = CookieHelper.getString (_tdsSettings.getCookieName ("Mode"));
    if (!StringUtils.isEmpty (modeName))
      return modeName;

    // TODO Shiva/Milan
    // return app setting or use the app name as the default
    // return AppSettings.get ("ModeName", _tdsSettings.getAppName ()).toString
    // ();
    return _tdsSettings.getAppName ();
  }

  public boolean setModeName (String modeName) {
    if (StringUtils.isEmpty (modeName))
      return false;
    CookieHelper.setValue (_tdsSettings.getCookieName ("Mode"), modeName);
    return true;
  }

  // / <summary>
  // / Get URL of welcome mat application, if we are in a distributed
  // architecture.
  // / return null otherwise.
  // / </summary>
  public String getTesteeCheckin () {
    // check if we should ignore checkin
    if (AppSettingsHelper.getBoolean ("testeeCheckin.disable"))
      return null;

    // check if we should override the checkin url
    String testeeCheckin = AppSettingsHelper.get ("testeeCheckin.overrideUrl");
    if (!StringUtils.isEmpty (testeeCheckin))
      return testeeCheckin;

    // get the check in url from externs table
    // IConfigRepository configRepository = ContextBeans.get
    // ("iConfigRepository", IConfigRepository.class);

    AppExterns appExterns = null;
    try {
      appExterns = _configRepository.getExterns ();
    } catch (ReturnStatusException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
    return appExterns.getTesteeCheckin ();

  }

  public boolean getInPTMode () {
    // IConfigRepository configRepository = ContextBeans.get
    // ("iConfigRepository", IConfigRepository.class);
    PTSetup ptSetup = null;
    try {
      ptSetup = _configRepository.getPTSetup ();
    } catch (ReturnStatusException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
    return (ptSetup != null) && ptSetup.inPTMode ();
  }

  // / <summary>
  // / If this is true then we use CLS or standalone proctor login instead of a
  // real proctored session.
  // / </summary>
  public boolean isProxyLogin () {
    // check if manually set to proxy login
    if (AppSettingsHelper.getBoolean ("ProxyLogin"))
      return true;

    String appName = _tdsSettings.getAppName ();
    return (appName != null && (appName.equalsIgnoreCase ("ResponseEntry") || appName.equalsIgnoreCase ("ScoreEntry") || appName.equalsIgnoreCase ("SIRVE")));

  }

  // / <summary>
  // / If this is true then we are performing data entry (score or response
  // entry).
  // / </summary>
  public boolean getIsDataEntry () {
    // check if manually set to data entry
    if (AppSettingsHelper.getBoolean ("DataEntry"))
      return true;

    String appName = _tdsSettings.getAppName ();
    // TODO
    return (appName != null && (appName.equalsIgnoreCase ("ResponseEntry") || appName.equalsIgnoreCase ("ScoreEntry")));

  }

  private final String COOKIE_ITEMSCORES = "TC_IS";

  // / <summary>
  // / If this is true the student app is in read only mode (can't respond to
  // tests).
  // / </summary>
  /*
   * public boolean getIsReadOnly () { // check if manually set to readonly if
   * (AppSettingsHelper.getBoolean ("ReadOnly")) return true;
   * 
   * // if we are showing item scores then we need to be in read only mode if
   * (getShowItemScores ()) return true;
   * 
   * // check app name return getIsSIRVE ();
   * 
   * }
   */

  // / <summary>
  // / If this is a SIRVE app: should be readonly and should have app name as
  // "SIRVE".
  // / </summary>
  public boolean getIsSIRVE () {
    // check app name
    String appName = _tdsSettings.getAppName ();
    return (appName != null) && appName.equalsIgnoreCase ("SIRVE");
  }

  // / <summary>
  // / If this is true then we are showing item scores and feedback in the test.
  // / </summary>
  // / <remarks>
  // / This usually gets set at the end of a PT if the accommodations allows for
  // it.
  // / </remarks>
  public boolean getShowItemScores () {
    String itemScores = StudentCookie.getCookieData (COOKIE_ITEMSCORES);
    if (itemScores != null && !itemScores.isEmpty ()) {
      return Boolean.valueOf (itemScores);
    } else {
      return false;
    }
  }

  public void setShowItemScores (boolean showItemScores) {
    StudentCookie.setCookieData (COOKIE_ITEMSCORES, String.valueOf (showItemScores));
  }

  public String getGenericErrorMessage () {
    // return "We were unable to process your request.";
    return "A problem was encountered while processing the request. You will be logged out.";
  }

  public String getAppName () {
    return _tdsSettings.getAppName ();
  }

  public boolean isReadOnly () {
    if (AppSettingsHelper.getBoolean ("ReadOnly"))
      return true;
    if (this.getShowItemScores ())
      return true;
    return this.getIsSIRVE ();
  }

  public Iterable<String> getComments() throws ReturnStatusException
  {
    return this._configRepository.getComments ();
  }
  
}
