/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import tds.exam.ExamStatusCode;

public class OpportunityStatusExtensions
{
  private static final Map<String, OpportunityStatusType> _statuses = new HashMap<String, OpportunityStatusType> ();
  private static final Map<String, OpportunityStatusType> examStatusToOpportunityStatus = new HashMap<>();

  public static OpportunityStatusType parse (String statusString) {
    OpportunityStatusType parsedStatusType = OpportunityStatusType.Unknown;

    if (!StringUtils.isEmpty (statusString)) {
      parsedStatusType = _statuses.get (statusString);
    }

    return parsedStatusType;
  }

  public static OpportunityStatusType parseExamStatus(String examStatusCode) {
    OpportunityStatusType parsedStatusType = OpportunityStatusType.Unknown;

    if (StringUtils.isNotEmpty(examStatusCode)) {
      parsedStatusType = examStatusToOpportunityStatus.get (examStatusCode);
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

    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_APPROVED, OpportunityStatusType.Approved);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_FAILED, OpportunityStatusType.Failed);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_PAUSED, OpportunityStatusType.Paused);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_PENDING, OpportunityStatusType.Pending);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_REVIEW, OpportunityStatusType.Review);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_STARTED, OpportunityStatusType.Started);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_SUSPENDED, OpportunityStatusType.Suspended);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_DENIED, OpportunityStatusType.Denied);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_COMPLETED, OpportunityStatusType.Completed);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_SCORED, OpportunityStatusType.Scored);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_SEGMENT_ENTRY, OpportunityStatusType.SegmentEntry);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_SEGMENT_EXIT, OpportunityStatusType.SegmentExit);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_CLOSED, OpportunityStatusType.Closed);
    examStatusToOpportunityStatus.put(ExamStatusCode.STATUS_DISABLED, OpportunityStatusType.Disabled);
  }
}
