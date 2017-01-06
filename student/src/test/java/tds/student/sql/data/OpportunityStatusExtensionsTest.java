package tds.student.sql.data;

import org.junit.Test;

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
import static tds.exam.ExamStatusCode.STATUS_SEGMENT_EXIT;
import static tds.exam.ExamStatusCode.STATUS_STARTED;
import static tds.exam.ExamStatusCode.STATUS_SUSPENDED;
import static tds.exam.ExamStatusCode.STAUTS_SEGMENT_ENTRY;
import static tds.exam.ExamStatusCode.STATUS_SCORED;


public class OpportunityStatusExtensionsTest {

  @Test
  public void shouldParseExamStatus() {
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_APPROVED)).isEqualTo(OpportunityStatusType.Approved);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_CLOSED)).isEqualTo(OpportunityStatusType.Closed);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_COMPLETED)).isEqualTo(OpportunityStatusType.Completed);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_DENIED)).isEqualTo(OpportunityStatusType.Denied);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_DISABLED)).isEqualTo(OpportunityStatusType.Disabled);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_REVIEW)).isEqualTo(OpportunityStatusType.Review);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_FAILED)).isEqualTo(OpportunityStatusType.Failed);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_PENDING)).isEqualTo(OpportunityStatusType.Pending);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_SUSPENDED)).isEqualTo(OpportunityStatusType.Suspended);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_SEGMENT_EXIT)).isEqualTo(OpportunityStatusType.SegmentExit);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STAUTS_SEGMENT_ENTRY)).isEqualTo(OpportunityStatusType.SegmentEntry);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_STARTED)).isEqualTo(OpportunityStatusType.Started);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_PAUSED)).isEqualTo(OpportunityStatusType.Paused);
    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_SCORED)).isEqualTo(OpportunityStatusType.Scored);

    assertThat(OpportunityStatusExtensions.parseExamStatus(STATUS_INITIALIZING)).isNull();
    assertThat(OpportunityStatusExtensions.parseExamStatus(null)).isEqualTo(OpportunityStatusType.Unknown);
  }

  @Test
  public void shouldParseStatusString() {
    assertThat(OpportunityStatusExtensions.parse("bogus")).isNull();
    assertThat(OpportunityStatusType.parse(null)).isEqualTo(OpportunityStatusType.Unknown);
  }
}