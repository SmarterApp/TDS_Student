/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

public class OpportunityStatusExtensions
{
  private static final HashMap<String, OpportunityStatusType> _statuses = new HashMap<String, OpportunityStatusType> ();

  public static OpportunityStatusType parse (String statusString) {
    OpportunityStatusType parsedStatusType = OpportunityStatusType.Unknown;

    if (!StringUtils.isEmpty (statusString)) {
      parsedStatusType = _statuses.get (statusString);
    }

    return parsedStatusType;
  }

  static {
    _statuses.put ("pending", OpportunityStatusType.Pending);
    _statuses.put ("denied", OpportunityStatusType.Denied);
    _statuses.put ("paused", OpportunityStatusType.Paused);
    _statuses.put ("approved", OpportunityStatusType.Approved);
    _statuses.put ("suspended", OpportunityStatusType.Suspended);
    _statuses.put ("started", OpportunityStatusType.Started);
    _statuses.put ("review", OpportunityStatusType.Review);
    _statuses.put ("completed", OpportunityStatusType.Completed);
    _statuses.put ("scored", OpportunityStatusType.Scored);
    _statuses.put ("failed", OpportunityStatusType.Failed);
    _statuses.put ("NA", OpportunityStatusType.NotApplicable);
    _statuses.put ("segmentEntry", OpportunityStatusType.SegmentEntry);
    _statuses.put ("segmentExit", OpportunityStatusType.SegmentExit);
    _statuses.put ("closed", OpportunityStatusType.Closed);
    _statuses.put ("disabled", OpportunityStatusType.Disabled);
  }
}
