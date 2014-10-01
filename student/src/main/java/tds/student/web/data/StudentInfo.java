/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.data;

import org.apache.commons.lang.StringUtils;

import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;
import tds.student.web.StudentContext;

/*
 * using System; using System.Web; using AIR.Common; using TDS.Shared.Messages;
 * using TDS.Student.Sql.Data;
 */
// / <summary>
// / Used for showing student information on the UI.
// / </summary>

public class StudentInfo
{
  public static String getFullName () {
    Testee testee = StudentContext.getTestee ();

    if (/*!HttpContext.getCurrentContext ().isAuthenticated () || */testee == null)
      return "Not Logged In";
    return String.format ("%s (<span i18n-content=\"Global.Label.SSID\"></span>: %s)", testee.getFullName (), testee.getId ());
  }

  public static String getSessionID () {
    TestSession session = StudentContext.getSession ();

    if (/*!HttpContext.getCurrentContext ().isAuthenticated () || */session == null)
      return StringUtils.EMPTY;
    return session.getId (); // TODO: Are we supposed to use this key?
                             // StudentMaster.Label.SessionID
  }
}
