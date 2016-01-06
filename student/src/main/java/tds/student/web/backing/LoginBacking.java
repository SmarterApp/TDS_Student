/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.backing;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import com.fasterxml.jackson.databind.ObjectMapper;

import tds.student.sbacossmerge.data.LoginInfo;
import tds.student.web.StudentContext;
import tds.student.web.StudentCookie;
import tds.student.web.StudentSettings;
import tds.student.web.backing.dummy.StudentPage;
import tds.student.web.controls.dummy.GlobalJavascript;
import AIR.Common.Json.JsonHelper;
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
  private boolean             _haveLoginData      = false;
  private GlobalJavascript _globalJavascript = null;
  
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

  public boolean doNothaveLoginData () {
    return !_haveLoginData;
  }

  public void setGlobalJavascript(GlobalJavascript value)
  {
    _globalJavascript = value;
  }

  
  public GlobalJavascript getGlobalJavascript()
  {
    return _globalJavascript;
  }

  @PostConstruct
  public void init () {
    pageInit ();
  }

  private void pageInit () {

    boolean logout = WebHelper.getQueryBoolean ("logout");

    try {
      // http://stackoverflow.com/questions/3337056/convenient-way-to-parse-incoming-multipart-form-data-parameters-in-a-servlet
      HttpServletRequest request = getRequest ();
      String loginInfoString = request.getParameter ("package");
      if (!StringUtils.isEmpty (loginInfoString)) {
        _haveLoginData = true;
        // parse loginInfoString into LoginInfo object.
        processLoginInfo (loginInfoString);
      }
    } catch (Exception exp) {
      exp.printStackTrace ();
    }

    
    // clear cookies
    if (logout || (WebHelper.getQueryString ("section") == null && !_haveLoginData)) {
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

  // TODO Shiva: verify logic in here especially related to login/TDSIdentity.
  private void processLoginInfo (String loginInfoString) {
    try {
      LoginInfo loginInfo = JsonHelper.deserialize (loginInfoString, LoginInfo.class);
      // check if login has expired
      if (loginInfo.isExpired ()) {
        // TODO Sajib
        // WriteError("Login timestamp is expired. Please use the login site.",
        // true);
        // return false;
      } else {
        StudentContext.saveTestee (loginInfo.getTestee ());
        StudentContext.saveSession (loginInfo.getSession ());
        StudentCookie.writeStore ();
      }
    } catch (Exception exp) {
      exp.printStackTrace ();
      // TODO Sajib: log this and redirect to loginshell.xhtml
    }

    /*
     * TODO Sajib // check for language in global accommodations if
     * (loginInfo.GlobalAccs != null && loginInfo.GlobalAccs.Count > 0) { var
     * langAcc = loginInfo.GlobalAccs.Find(acc => acc.Type == "Language"); if
     * (langAcc != null && langAcc.Codes != null && langAcc.Codes.Count > 0) {
     * // set language so GlobalJavascript.cs sends down the right message
     * translations StudentContext.SetLanguage(langAcc.Codes[0]); } }
     */

    // TODO Shiva: Check this.
    // sign in user on this server
    // TDSIdentity.New(loginInfo.Testee.ID, new WebValueCollection());

    StringBuilder sb = new StringBuilder ();
    sb.append ("var tdsLoginInfo = /*LOGIN INFO START*/ ");
    sb.append (loginInfoString);
    sb.append (" /*LOGIN INFO END*/ ;");
    sb.append ("\n");

    getClientScript ().addToJsCode (sb.toString ());
  }
}
