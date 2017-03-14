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
