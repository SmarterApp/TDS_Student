/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.student.services.remote;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import tds.exam.ExamStatusCode;
import tds.student.sql.data.OpportunityStatusType;

class ExamStatusMapper {
  private static final Map<String, OpportunityStatusType> examStatusToOpportunityStatus = new HashMap<>();

  static {
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

  static OpportunityStatusType parseExamStatus(final String examStatusCode) {
    OpportunityStatusType parsedStatusType = OpportunityStatusType.Unknown;

    if (StringUtils.isNotEmpty(examStatusCode)) {
      parsedStatusType = examStatusToOpportunityStatus.get (examStatusCode);
    }

    return parsedStatusType;
  }
}
