/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.Utilities.SpringApplicationContext;
import AIR.Common.Web.CookieHelper;
import AIR.Common.Web.Session.HttpContext;
import AIR.Common.Web.Session.MultiValueCookie;
import TDS.Shared.Configuration.TDSSettings;

/**
 * @author mpatel
 * 
 */
// / <summary>
// / Strongly typed cookie storage for general test information. This is NOT
// encrypted.
// / </summary>
// / <remarks>
// / Allowed characters in cookies:
// / http://stackoverflow.com/questions/1969232/allowed-characters-in-cookies
// / </remarks>
public class StudentCookie
{

  private static final Logger _logger = LoggerFactory.getLogger (StudentCookie.class);

  public static boolean getExcludeSchool () {
    String excludeSchool = getCookieData ("ExcludeSchool");
    return Boolean.valueOf (excludeSchool);
  }

  public static void setExcludeSchool (boolean excludeSchool) {
    setCookieData ("ExcludeSchool", String.valueOf (excludeSchool));
  }

  public static String getTestAccommodations () {
    return getCookieDirect ("TestAccs");
  }

  public static void setTestAccommodations (String testAccommodations) {
    setCookieDirect ("TestAccs", testAccommodations);
  }

  // / <summary>
  // / Get a value from the main cookie collection (a single cookie that holds
  // multiple values)
  // / </summary>
  public static String getCookieData (String name) {
    MultiValueCookie cookie = getCookieContainer ();
    return cookie.getValue (name);
  }

  // / <summary>
  // / Get a value from the main cookie collection (a single cookie that holds
  // multiple values)
  // / </summary>
  public static int getCookieDataInt (String name) {
    String value = getCookieData (name);
    if (StringUtils.isEmpty (value)) {
      return 0;
    }
    return Integer.parseInt (value);
  }

  // / <summary>
  // / Get a value from the main cookie collection (a single cookie that holds
  // multiple values)
  // / </summary>
  public static boolean getCookieDataBoolean (String name) {
    String value = getCookieData (name);
    if (StringUtils.isEmpty (value)) {
      return false;
    }
    return Boolean.parseBoolean (value);
  }

  public static MultiValueCookie getCookieByName (String name) {
    return HttpContext.getCurrentContext ().getCookies ().findCookie (name);
  }

  public static boolean hasCookieData (String name) {
    if (HttpContext.getCurrentContext () == null)
      return false;

    MultiValueCookie container = getCookieContainer ();
    return container != null && container.isCookieExists (name);
  }

  // / <summary>
  // / Set a value into the main cookie collection (a single cookie that holds
  // multiple values)
  // / </summary>
  public static void setCookieData (String name, Object value) {
    MultiValueCookie cookie = getCookieContainer ();
    cookie.setValue (name, value.toString ());
    // this indicates to our API to add the cookie to the response.
    HttpContext.getCurrentContext ().getCookies ().addToStore (cookie);
  }

  public static void deleteCookie (String name) {
    String cookieName = getCookieName (name);
    CookieHelper.deleteCookie (cookieName);
  }

  public static void clear () {
    // clear container
    MultiValueCookie container = getCookieContainer ();
    if(container!=null) {
      container.clear ();
    }

    List<MultiValueCookie> toBeDeleted = new ArrayList<MultiValueCookie> ();
    // clear request cookies
    for (MultiValueCookie cookie : HttpContext.getCurrentContext ().getCookies ()) {

      if (cookie != null && cookie.getName ().contains ("TDS-Student-")) {
        // don't delete the client cookie
        if (cookie.getName () == SpringApplicationContext.getBean ("tdsSettings", TDSSettings.class).getCookieName ("Client")) {
          continue;
        }
        // track cookie to be deleted and deleted in a separate loop. see below.
        toBeDeleted.add (cookie);
      }
    }

    // deletion here means that we update the expiry time of the cookie and add
    // it back to the response.
    // hence we will have to delete in a separate loop as otherwise we would get
    // a concurrent modification exception.
    for (MultiValueCookie cookie : toBeDeleted) {
      try {
        CookieHelper.deleteCookie (cookie);
      } catch (Exception e) {
        _logger.error ("Error deleting cookie", e);
      }
    }

  }

  private static String getCookieName (String name) {
    return "TDS-Student-" + name;
  }

  // / <summary>
  // / Get the current cookie container for the TDS data
  // / </summary>
  private static MultiValueCookie getCookieContainer () {
    if (HttpContext.getCurrentContext ().getCookies ().findCookie ("TDS-Student-Data") == null) {
      MultiValueCookie cookie = new MultiValueCookie ("TDS-Student-Data");
      HttpContext.getCurrentContext ().getCookies ().addToStore (cookie);
    }
    return (MultiValueCookie) HttpContext.getCurrentContext ().getCookies ().findCookie ("TDS-Student-Data");
  }

  private static <T> T getCookieDirect (String name) {
    T value = null;
    try {
      value = CookieHelper.getValue (getCookieName (name), value);
    } catch (Exception e) {
      e.printStackTrace ();
      _logger.error (e.getMessage (), e);
    }

    return value;
  }

  private static void setCookieDirect (String name, Object value) {
    if (HttpContext.getCurrentContext () == null)
      return;

    String cookieName = getCookieName (name);

    if (value == null) {
      deleteCookie (cookieName);
    } else {
      HttpContext.getCurrentContext ().getCookies ().addToStore (new MultiValueCookie (cookieName, value.toString ()));
    }
  }
  
  public static void writeStore() {
	  HttpContext.getCurrentContext().getCookies().writeStore();
  }
}
