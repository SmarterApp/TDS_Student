/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;

import AIR.Common.Web.Session.HttpContext;

/**
 * @author temp_rreddy
 * 
 */
public class BrowserInfoCookie
{
  private static String _cookiename = "TDS-Student-Browser";

  // A subcookie string looks similar to a URL and takes the following form:
  // cookiename=name1=value1&name2=value2&name3=value3
  // TODO mpatel - Implement Cookie as per above comment
  private static String GetCookieSub (String subName) {
    HttpContext context = HttpContext.getCurrentContext ();
    if (context == null)
      return "";

    Cookie clientInfoCookie = null;
    for (Cookie cookie : context.getRequest ().getCookies ()) {
      if (cookie.getName ().equals (_cookiename)) {
        clientInfoCookie = cookie;
        break;
      }
    }

    if (clientInfoCookie == null)
      return "";

    // String value = clientInfoCookie[subName];
    String value = clientInfoCookie.getValue ();
    // TODO mpatel - value might be in this form
    // name1=value1&name2=value2&name3=value3 <Process accordingly>

    if (!StringUtils.isEmpty (value)) {
      // NOTE: YUI setSub encodes values
      try {
        value = URLDecoder.decode (value, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace ();
      }
    }

    return value;
  }

  public static String getScreen () {
    return GetCookieSub ("screen");
  }

  public static String getLocalIP () {
    return GetCookieSub ("ip");
  }

  public static String getMACAddress () {
    return GetCookieSub ("mac");
  }

  public static String getTTS () {
    return GetCookieSub ("tts");
  }

}
