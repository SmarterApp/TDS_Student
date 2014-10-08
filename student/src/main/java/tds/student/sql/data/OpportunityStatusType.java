/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

// / <summary>
// / A list of all possible return status or opp repository GetStatus
// function.
// / </summary>
public enum OpportunityStatusType {
  // / <summary>
  // / The opportunity status has not or could not be parsed.
  // / </summary>
  Unknown(0),

  Denied(-1), Failed(-2), Pending(1), Suspended(2), Approved(3), Started(4), Paused(5), Review(6), Completed(7), Scored(8), NotApplicable(9), SegmentEntry(10), SegmentExit(11), Closed(12),

  // / <summary>
  // / The opportunity is disabled in the test selection screen.
  // / </summary>
  Disabled(13);
  private int _code;

  OpportunityStatusType (int code) {
    _code = code;
  }

  public int getCode () {
    return _code;
  }

  public static OpportunityStatusType parse (String type)
  {
    return OpportunityStatusExtensions.parse (type);
  }
}
