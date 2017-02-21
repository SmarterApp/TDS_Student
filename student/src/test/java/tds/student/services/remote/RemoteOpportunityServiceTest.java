package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamConfiguration;
import tds.exam.ExamSegment;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.data.ApprovalInfo;
import tds.student.sql.abstractions.ExamRepository;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunitySegment;
import tds.student.sql.data.OpportunityStatus;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.OpportunityStatusExtensions;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteOpportunityServiceTest {
  @Mock
  private IOpportunityService legacyOpportunityService;

  private IOpportunityService service;

  @Mock
  private ExamRepository examRepository;

  @Before
  public void setUp() {
    service = new RemoteOpportunityService(legacyOpportunityService, true, true, examRepository);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void shouldReturnTestOpportunityBasedOnExam() throws ReturnStatusException {
    UUID browserKey = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    Exam exam = new Exam.Builder()
      .withId(examId)
      .withBrowserId(browserKey)
      .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.IN_PROGRESS), Instant.now())
      .build();
    Response<Exam> response = new Response<>(exam);

    when(examRepository.openExam(isA(OpenExamRequest.class))).thenReturn(response);

    TestSession session = new TestSession();
    Testee testee = new Testee();

    when(legacyOpportunityService.openTest(testee, session, "testKey")).thenReturn(new OpportunityInfo());

    OpportunityInfo info = service.openTest(testee, session, "testKey");
    verify(legacyOpportunityService).openTest(testee, session, "testKey");

    assertThat(info.getExamBrowserKey()).isEqualTo(browserKey);
    assertThat(info.getExamId()).isEqualTo(examId);
    assertThat(info.getExamStatus()).isEqualTo(OpportunityStatusType.Approved);
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowExceptionWhenErrorsArePresent() throws ReturnStatusException {
    ValidationError error = new ValidationError("TEST", "TEST");

    Response<Exam> response = new Response<>(error);
    when(examRepository.openExam(isA(OpenExamRequest.class))).thenReturn(response);

    TestSession session = new TestSession();
    Testee testee = new Testee();

    service.openTest(testee, session, "testKey");
    verify(legacyOpportunityService).openTest(testee, session, "testKey");
  }

  @Test
  public void shouldNotExecuteRemoteCallIfNotEnabled() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, false, true, examRepository);

    Testee testee = new Testee();
    TestSession testSession = new TestSession();
    service.openTest(testee, testSession, "testKey");
    verify(legacyOpportunityService).openTest(testee, testSession, "testKey");

    verifyZeroInteractions(examRepository);
  }

  @Test
  public void shouldNotExecuteLegacyCallIfNotEnabled() throws ReturnStatusException {
    Exam exam = new Exam.Builder()
      .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.IN_PROGRESS), Instant.now())
      .build();
    Response<Exam> response = new Response<>(exam);

    when(examRepository.openExam(isA(OpenExamRequest.class))).thenReturn(response);

    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);

    Testee testee = new Testee();
    TestSession testSession = new TestSession();
    service.openTest(testee, testSession, "testKey");
    verify(examRepository).openExam(isA(OpenExamRequest.class));

    verifyZeroInteractions(legacyOpportunityService);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowIfBothImplementationsAreDisabled() {
    new RemoteOpportunityService(legacyOpportunityService, false, false, examRepository);
  }

  @Test
  public void shouldGetApprovedStatusNoErrors() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamApproval examApproval = new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), null);

    when(examRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey())).thenReturn(
      new Response(examApproval));
    OpportunityStatus retStatus = service.getStatus(oppInstance);
    assertThat(retStatus.getStatus().toString()).isEqualTo("Approved");
    verify(examRepository).getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());
  }

  @Test
  public void shouldGetDeniedStatusNoErrors() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamApproval examApproval = new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_DENIED, ExamStatusStage.OPEN), null);

    when(examRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey())).thenReturn(
      new Response(examApproval));
    OpportunityStatus retStatus = service.getStatus(oppInstance);
    assertThat(retStatus.getStatus().toString()).isEqualTo("Denied");
    verify(examRepository).getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowWithErrorsPresent() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamApproval examApproval = new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_DENIED, ExamStatusStage.OPEN), null);

    when(examRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey())).thenReturn(
      new Response(examApproval, new ValidationError("Some Error", "ErrorCode")));
    OpportunityStatus retStatus = service.getStatus(oppInstance);
  }

  @Test
  public void shouldReturnApprovalInfo() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamApproval examApproval = new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), null);

    when(examRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey())).thenReturn(
      new Response(examApproval));
    ApprovalInfo approvalInfo = service.checkTestApproval(oppInstance);
    assertThat(approvalInfo.getStatus().toString()).isEqualTo("Approved");
    verify(examRepository).getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());
  }

  @Test
  public void shouldDenyApproval() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);

    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

    //mock getStatus
    when(examRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey()))
      .thenReturn(new Response<>(new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_APPROVED), "reason")));

    final String deniedStatus = ExamStatusCode.STATUS_DENIED;
    OpportunityStatusChange statusChange = new OpportunityStatusChange(OpportunityStatusType.Pending, true, deniedStatus);
    when(examRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name(), deniedStatus)).thenReturn(Optional.<ValidationError>absent());
    service.denyApproval(oppInstance);

    verify(examRepository).getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());
    verify(examRepository).updateStatus(oppInstance.getExamId(), statusChange.getStatus().name(), deniedStatus);
  }

  @Test
  public void shouldNotSetStatusIfStatusIsPaused() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    OpportunityStatus currentStatus = new OpportunityStatus();
    currentStatus.setStatus(OpportunityStatusType.Paused);

    //mock getStatus
    when(examRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey()))
      .thenReturn(new Response<>(new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_PAUSED), "reason")));

    service.denyApproval(oppInstance);

    verify(examRepository).getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());
    verify(examRepository, never()).updateStatus((UUID) any(), (String) any(), (String) any());
  }

  @Test
  public void shouldUpdateStatus() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);

    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    OpportunityStatus currentStatus = new OpportunityStatus();
    currentStatus.setStatus(OpportunityStatusType.Approved);

    final String deniedStatus = ExamStatusCode.STATUS_DENIED;
    OpportunityStatusChange statusChange = new OpportunityStatusChange(OpportunityStatusType.Pending, true, deniedStatus);
    when(examRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name(), deniedStatus)).thenReturn(Optional.<ValidationError>absent());
    boolean isApproved = service.setStatus(oppInstance, statusChange);
    assertThat(isApproved).isTrue();
    verify(examRepository).updateStatus(oppInstance.getExamId(), statusChange.getStatus().name(), deniedStatus);
  }

  @Test
  public void shouldReturnTrueForErrorWithFalseCheckStatus() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);

    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    OpportunityStatus currentStatus = new OpportunityStatus();
    currentStatus.setStatus(OpportunityStatusType.Approved);

    final String deniedStatus = ExamStatusCode.STATUS_DENIED;
    OpportunityStatusChange statusChange = new OpportunityStatusChange(OpportunityStatusType.Pending, false, deniedStatus);
    when(examRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name(), deniedStatus))
      .thenReturn(Optional.of(new ValidationError("Error", "Code")));
    boolean isApproved = service.setStatus(oppInstance, statusChange);
    assertThat(isApproved).isTrue();
    verify(examRepository).updateStatus(oppInstance.getExamId(), statusChange.getStatus().name(), deniedStatus);
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowForReturnStatusFailed() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);

    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    OpportunityStatus currentStatus = new OpportunityStatus();
    currentStatus.setStatus(OpportunityStatusType.Approved);

    final String deniedStatus = ExamStatusCode.STATUS_DENIED;
    OpportunityStatusChange statusChange = new OpportunityStatusChange(OpportunityStatusType.Pending, true, deniedStatus);
    when(examRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name(), deniedStatus))
      .thenReturn(Optional.of(new ValidationError(ExamStatusCode.STATUS_FAILED, "There was an error!")));
    service.setStatus(oppInstance, statusChange);
  }

  @Test
  public void shouldReturnFalseForValidationErrorReturned() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);

    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    OpportunityStatus currentStatus = new OpportunityStatus();
    currentStatus.setStatus(OpportunityStatusType.Approved);

    final String deniedStatus = ExamStatusCode.STATUS_DENIED;
    OpportunityStatusChange statusChange = new OpportunityStatusChange(OpportunityStatusType.Pending, true, deniedStatus);
    when(examRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name(), deniedStatus))
      .thenReturn(Optional.of(new ValidationError("Another Error", "There was an error!")));
    boolean isApproved = service.setStatus(oppInstance, statusChange);
    assertThat(isApproved).isFalse();
    verify(examRepository).updateStatus(oppInstance.getExamId(), statusChange.getStatus().name(), deniedStatus);
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowForErrorPresent() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    final String assessmentKey = "assessmentKey";
    Response<ExamConfiguration> errorResponse = new Response<>(new ValidationError("uh", "oh!"));

    when(examRepository.startExam(oppInstance.getExamId())).thenReturn(errorResponse);
    service.startTest(oppInstance, assessmentKey, null);
  }

  @Test
  public void shouldStartExamAndReturnTestConfig() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    final String assessmentKey = "assessmentKey";

    ExamConfiguration mockExamConfig = new ExamConfiguration.Builder()
      .withExam(
        new Exam.Builder()
          .withId(oppInstance.getExamId())
          .withRestartsAndResumptions(3)
          .build())
      .withExamRestartWindowMinutes(120)
      .withContentLoadTimeout(10)
      .withInterfaceTimeout(3)
      .withRequestInterfaceTimeout(90)
      .withPrefetch(2)
      .withStatus(ExamStatusCode.STATUS_STARTED)
      .withTestLength(10)
      .withStartPosition(1)
      .withExamRestartWindowMinutes(60)
      .build();

    when(examRepository.startExam(oppInstance.getExamId())).thenReturn(new Response<>(mockExamConfig));
    TestConfig testConfig = service.startTest(oppInstance, assessmentKey, null);
    verify(examRepository).startExam(oppInstance.getExamId());

    assertThat(testConfig).isNotNull();
    assertThat(testConfig.getStatus()).isEqualTo(OpportunityStatusExtensions.parseExamStatus(ExamStatusCode.STATUS_STARTED));
    assertThat(testConfig.getPrefetch()).isEqualTo(mockExamConfig.getPrefetch());
    assertThat(testConfig.getContentLoadTimeout()).isEqualTo(mockExamConfig.getContentLoadTimeoutMinutes());
    assertThat(testConfig.getInterfaceTimeout()).isEqualTo(mockExamConfig.getInterfaceTimeoutMinutes());
    assertThat(testConfig.getOppRestartMins()).isEqualTo(mockExamConfig.getExamRestartWindowMinutes());
    assertThat(testConfig.getRequestInterfaceTimeout()).isEqualTo(mockExamConfig.getRequestInterfaceTimeoutMinutes());
    assertThat(testConfig.getRestart()).isEqualTo(mockExamConfig.getExam().getRestartsAndResumptions());
    assertThat(testConfig.getStartPosition()).isEqualTo(mockExamConfig.getStartPosition());
  }

  @Test
  public void shouldFindExamSegmentsForExamIds() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamSegment seg1 = new ExamSegment.Builder()
      .withSegmentKey("seg1")
      .withSegmentId("seg1ID")
      .withFormKey("seg1formkey")
      .withFormId("seg1formid")
      .withPermeable(false)
      .withExamId(oppInstance.getExamId())
      .withSegmentPosition(1)
      .withRestorePermeableCondition("Neva!")
      .withFieldTestItemCount(0)
      .build();
    ExamSegment seg2 = new ExamSegment.Builder()
      .withSegmentKey("seg2")
      .withSegmentId("seg2ID")
      .withFormKey("seg2formkey")
      .withFormId("seg2formid")
      .withExamId(oppInstance.getExamId())
      .withSegmentPosition(2)
      .withRestorePermeableCondition("Neva!")
      .withPermeable(true)
      .withFieldTestItemCount(4)
      .build();

    when(examRepository.findExamSegments(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey()))
      .thenReturn(new Response<>(Arrays.asList(seg1, seg2)));
    OpportunitySegment.OpportunitySegments opportunitySegments = service.getSegments(oppInstance, true);
    verify(examRepository).findExamSegments(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());

    assertThat(opportunitySegments).isNotNull();
    OpportunitySegment oppSeg1 = null;
    OpportunitySegment oppSeg2 = null;

    for (OpportunitySegment oppSeg : opportunitySegments) {
      if (oppSeg.getKey().equals(seg1.getSegmentKey())) {
        oppSeg1 = oppSeg;
      } else if (oppSeg.getKey().equals((seg2.getSegmentKey()))) {
        oppSeg2 = oppSeg;
      }
    }

    assertThat(oppSeg1.getId()).isEqualTo(seg1.getSegmentId());
    assertThat(oppSeg1.getFormID()).isEqualTo(seg1.getFormId());
    assertThat(oppSeg1.getFormKey()).isEqualTo(seg1.getFormKey());
    assertThat(oppSeg1.getPosition()).isEqualTo(seg1.getSegmentPosition());
    assertThat(oppSeg1.getIsPermeable()).isEqualTo(-1);
    assertThat(oppSeg1.getRestorePermOn()).isEqualTo(seg1.getRestorePermeableCondition());
    assertThat(oppSeg1.getFtItems()).isEqualTo(String.valueOf(seg1.getFieldTestItemCount()));

    assertThat(oppSeg2.getId()).isEqualTo(seg2.getSegmentId());
    assertThat(oppSeg2.getFormID()).isEqualTo(seg2.getFormId());
    assertThat(oppSeg2.getFormKey()).isEqualTo(seg2.getFormKey());
    assertThat(oppSeg2.getPosition()).isEqualTo(seg2.getSegmentPosition());
    assertThat(oppSeg2.getIsPermeable()).isEqualTo(1);
    assertThat(oppSeg2.getRestorePermOn()).isEqualTo(seg2.getRestorePermeableCondition());
    assertThat(oppSeg2.getFtItems()).isEqualTo(String.valueOf(seg2.getFieldTestItemCount()));
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowForErrorsPresentFindExamSegments() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, true, false, examRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    when(examRepository.findExamSegments(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey()))
      .thenReturn(new Response<List<ExamSegment>>(new ValidationError("why", "not")));
    service.getSegments(oppInstance, true);
  }
}








