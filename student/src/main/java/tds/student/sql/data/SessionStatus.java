/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

/**
 * @author temp_rreddy
 * 
 */
import java.util.HashMap;

public class SessionStatus
{
  public enum SessionStatusType {
    Unknown, Open, Closed, Denied
  }

  private static final HashMap<String, SessionStatusType> _statusList = new HashMap<String, SessionStatusType> ();

  static {
    _statusList.put ("open", SessionStatusType.Open);
    _statusList.put ("closed", SessionStatusType.Closed);
    _statusList.put ("denied", SessionStatusType.Denied);
  }

  // / <summary>
  // / Parses a session status string into strongly typed enum
  // / </summary>
  // / <param name="statusString">The string version of the status </param>
  public static SessionStatusType Parse (String statusString) {
    SessionStatusType statusType = null;
    statusType = _statusList.get (statusString);
    return statusType;
  }
}
