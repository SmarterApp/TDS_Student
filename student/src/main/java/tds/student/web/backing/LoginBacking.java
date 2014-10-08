/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.backing;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;

import tds.student.web.StudentCookie;
import tds.student.web.StudentSettings;
import tds.student.web.backing.dummy.StudentPage;
import AIR.Common.Web.WebHelper;
import AIR.Common.Web.Session.Server;
import TDS.Shared.Security.FormsAuthentication;
import TDS.Shared.Web.BasePage;

public class LoginBacking extends StudentPage
{

  // Strings used for satellite logic. Some of these are names of JS variables.
  // If you change these, change the strings in JS login section, too.
  private static final String loginField1         = "loginRow_FirstName";
  private static final String loginField2         = "loginRow_ID";
  private static final String sessionIDField      = "loginSessionID";
  private static final String redirectSource      = "distributionHome";
  private static final String forceRedirectString = "forceRedirect";
  private static final String redirectObjectName  = "globalRedirectSettings";
  private static final String appString           = "accommodationStringP";

  public void setClientStylePath (String value) {

  }

  public String getClientStylePath () {
    return getTdsSettings ().getAppName ();
  }

  public String resolveUrl (String url) {
    return Server.resolveUrl (url);
  }

  // TODO: Shajib revisit this method to implement
  public boolean isPrefetchEnabled () {
    return false; // return ResourcesSingleton.isPrefetchEnabled()
  }

  // TODO: Shajib revisit this method to implement
  public boolean isProxyLogin () {
    return false;// return StudentSettings.isProxyLogin()
  }

  @PostConstruct
  public void init () {
    pageInit ();
  }

  private void pageInit () {
    boolean logout = WebHelper.getQueryBoolean ("logout");

    // clear cookies
    if (logout || WebHelper.getQueryString ("section") == null) {
      // NOTE: clearing cookies use to go here but not sure why?
      StudentCookie.clear ();
      FormsAuthentication.signOut ();
    }

    // set client cookie
    persistClientName ();

    // set mode name
    persistModeName ();

    // check if allowed browser
    checkBrowserAllowed ();

    // Are we a satellite here from welcome mat?
    checkForRedirect (logout);
  }

  // / <summary>
  // / If there is a client in the querystring persist this in a cookie.
  // / </summary>
  private void persistClientName () {
    // check if query string has a client name set
    String clientName = getTdsSettings ().getClientNameFromQueryString ();

    // if client name is in URL then save it in cookie
    if (!StringUtils.isEmpty (clientName)) {
      getTdsSettings ().setClientName (clientName);
    }
  }

  private void persistModeName () {
    // check if query string has a mode name set
    String modeName = WebHelper.getQueryString ("mode");

    if (!StringUtils.isEmpty (modeName)) {
      StudentSettings settings = getBean ("studentSettings", StudentSettings.class);
      settings.setModeName (modeName);
    }
  }

  // / <summary>
  // / Check if the student is allowed to see the login shell.
  // / </summary>
  private void checkBrowserAllowed () {
    // TODO Shiva not implemented.
  }

  // / <summary>
  // / This function does double duty. If the user was redirected here from the
  // distrubution site,
  // / write the POST variables to the test shell. If the user is here for a
  // logout or normal GET,
  // / and force redirect setting is set, cause the shell to redirect them.
  // / </summary>
  // / <param name="logout"></param>
  public void checkForRedirect (boolean logout) {
    // TODO shiva
  }
}
