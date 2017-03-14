package tds.student.services.remote;

import org.junit.Test;

import tds.student.sql.data.OpportunityStatusType;

import static org.assertj.core.api.Assertions.assertThat;
import static tds.exam.ExamStatusCode.STATUS_APPROVED;
import static tds.exam.ExamStatusCode.STATUS_CLOSED;
import static tds.exam.ExamStatusCode.STATUS_COMPLETED;
import static tds.exam.ExamStatusCode.STATUS_DENIED;
import static tds.exam.ExamStatusCode.STATUS_DISABLED;
import static tds.exam.ExamStatusCode.STATUS_FAILED;
import static tds.exam.ExamStatusCode.STATUS_INITIALIZING;
import static tds.exam.ExamStatusCode.STATUS_PAUSED;
import static tds.exam.ExamStatusCode.STATUS_PENDING;
import static tds.exam.ExamStatusCode.STATUS_REVIEW;
import static tds.exam.ExamStatusCode.STATUS_SCORED;
import static tds.exam.ExamStatusCode.STATUS_SEGMENT_ENTRY;
import static tds.exam.ExamStatusCode.STATUS_SEGMENT_EXIT;
import static tds.exam.ExamStatusCode.STATUS_STARTED;
import static tds.exam.ExamStatusCode.STATUS_SUSPENDED;

public class ExamStatusMapperTest {
  @Test
  public void shouldParseExamStatus() {
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_APPROVED)).isEqualTo(OpportunityStatusType.Approved);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_CLOSED)).isEqualTo(OpportunityStatusType.Closed);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_COMPLETED)).isEqualTo(OpportunityStatusType.Completed);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_DENIED)).isEqualTo(OpportunityStatusType.Denied);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_DISABLED)).isEqualTo(OpportunityStatusType.Disabled);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_REVIEW)).isEqualTo(OpportunityStatusType.Review);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_FAILED)).isEqualTo(OpportunityStatusType.Failed);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_PENDING)).isEqualTo(OpportunityStatusType.Pending);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_SUSPENDED)).isEqualTo(OpportunityStatusType.Suspended);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_SEGMENT_EXIT)).isEqualTo(OpportunityStatusType.SegmentExit);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_SEGMENT_ENTRY)).isEqualTo(OpportunityStatusType.SegmentEntry);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_STARTED)).isEqualTo(OpportunityStatusType.Started);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_PAUSED)).isEqualTo(OpportunityStatusType.Paused);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_SCORED)).isEqualTo(OpportunityStatusType.Scored);
    assertThat(ExamStatusMapper.parseExamStatus(STATUS_INITIALIZING)).isNull();
    assertThat(ExamStatusMapper.parseExamStatus(null)).isEqualTo(OpportunityStatusType.Unknown);
  }
}