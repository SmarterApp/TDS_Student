/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import tds.student.proxy.data.Proctor;
import AIR.Common.Web.Session.HttpContext;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Web.UserCookie;

/**
 * @author temp_rreddy
 * 
 */
public class ProxyContext
{
  
  @Autowired
  private static TDSSettings tdsSettings;
  
  private static String _userInfoCookieName = tdsSettings.getCookieName ("ProctorInfo");

  private static UserCookie GetUserInfoCookie ()
  {
    UserCookie _userInfoCookie = new UserCookie (HttpContext.getCurrentContext (), _userInfoCookieName);
    return _userInfoCookie;
  }

  public static Proctor GetProctor ()
  {
    Proctor proctorUser = new Proctor (null, UUID.randomUUID ());

    UserCookie userCookie = GetUserInfoCookie ();
    proctorUser.setId (userCookie.GetValue ("userID"));

    long key = 0;
    key = Long.parseLong (userCookie.GetValue ("userKey"));
    proctorUser.setKey (key);

    proctorUser.setFullname (userCookie.GetValue ("fullName"));

    boolean isAuth;
    isAuth = Boolean.parseBoolean (userCookie.GetValue ("isAuth"));
    proctorUser.setAuth (isAuth);

    String browserKeyValue = userCookie.GetValue ("browserKey");

    // check if browser key exists
    if (!StringUtils.isEmpty (browserKeyValue))
    {
      UUID uuid = UUID.fromString (browserKeyValue);
      proctorUser.setBrowserKey (uuid);
    }

    proctorUser.setClientName ("");
    String proctorClientValue = userCookie.GetValue ("clientName");
    if (!StringUtils.isEmpty (proctorClientValue))
      proctorUser.setClientName (proctorClientValue);

    // restore session info.
    proctorUser.setSessionName (userCookie.GetValue ("sn"));
    proctorUser.setSessionId (userCookie.GetValue ("sid"));

    String sessionKey = userCookie.GetValue ("sk");
    // check if browser key exists
    if (!StringUtils.isEmpty (sessionKey))
    {
      UUID uuid = UUID.fromString (sessionKey);
      proctorUser.setSessionKey (uuid);
    }

    return proctorUser;
  }

  public static void SaveToCookie (Proctor proctor)
  {
    UserCookie userCookie = GetUserInfoCookie ();
    userCookie.SetValue ("userID", proctor.getId ());
    userCookie.SetValue ("userKey", proctor.getKey () + "");
    userCookie.SetValue ("fullName", proctor.getFullname ());
    userCookie.SetValue ("isAuth", proctor.isAuth () + "");
    userCookie.SetValue ("browserKey", proctor.getBrowserKey ().toString ());
    userCookie.SetValue ("clientName", proctor.getClientName ());
    // save session information.
    userCookie.SetValue ("sn", proctor.getSessionName ());
    userCookie.SetValue ("sid", proctor.getSessionId ());
    userCookie.SetValue ("sk", proctor.getSessionKey ().toString ());
  }

  public static boolean Logout ()
  {
    /*
     * SB09122011 - Talked with Larry and his advise was not to call
     * P_ProctorLogout when logging out this proctor. the reason is proxy app is
     * a second class app and call this will close first class sessions.
     */
    // ProctorService proctorService = ServiceLocator.Resolve<ProctorService>();
    // proctorService.Logout(this);
    UserCookie.RemoveCookie (HttpContext.getCurrentContext (), _userInfoCookieName);
    return true;
  }
}
