package tds.student.services.remote;

import TDS.Shared.Browser.BrowserAction;
import TDS.Shared.Browser.BrowserInfo;
import TDS.Shared.Browser.BrowserRule;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamAssessmentMetadata;
import tds.exam.ExamConfiguration;
import tds.exam.ExamSegment;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.exam.SegmentApprovalRequest;
import tds.student.performance.dao.TestOpportunityExamMapDao;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.data.ApprovalInfo;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunitySegment;
import tds.student.sql.data.OpportunityStatus;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestSegment;
import tds.student.sql.data.TestSelection;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;
import tds.student.sql.repository.ConfigRepository;
import tds.student.sql.repository.remote.ExamRepository;
import tds.student.sql.repository.remote.ExamSegmentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteOpportunityServiceTest {
  @Mock
  private IOpportunityService mockLegacyOpportunityService;

  private IOpportunityService service;

  @Mock
  private ExamRepository mockExamRepository;

  @Mock
  private ExamSegmentRepository mockExamSegmentRepository;

  @Mock
  private TestOpportunityExamMapDao mockTestOpportunityExamMapDao;

  @Mock
  private ConfigRepository configRepository;

  @Captor
  private ArgumentCaptor<SegmentApprovalRequest> segmentApprovalRequestArgumentCaptor;

  @Before
  public void setUp() {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, true,
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
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

    when(mockExamRepository.openExam(isA(OpenExamRequest.class))).thenReturn(response);

    TestSession session = new TestSession();
    Testee testee = new Testee();

    when(mockLegacyOpportunityService.openTest(testee, session, "testKey")).thenReturn(new OpportunityInfo());

    OpportunityInfo info = service.openTest(testee, session, "testKey");
    verify(mockLegacyOpportunityService).openTest(testee, session, "testKey");

    assertThat(info.getExamBrowserKey()).isEqualTo(browserKey);
    assertThat(info.getExamId()).isEqualTo(examId);
    assertThat(info.getExamStatus()).isEqualTo(OpportunityStatusType.Approved);
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowExceptionWhenErrorsArePresent() throws ReturnStatusException {
    ValidationError error = new ValidationError("TEST", "TEST");

    Response<Exam> response = new Response<>(error);
    when(mockExamRepository.openExam(isA(OpenExamRequest.class))).thenReturn(response);

    TestSession session = new TestSession();
    Testee testee = new Testee();

    service.openTest(testee, session, "testKey");
    verify(mockLegacyOpportunityService).openTest(testee, session, "testKey");
  }

  @Test
  public void shouldNotExecuteRemoteCallIfNotEnabled() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, false, true,
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);

    Testee testee = new Testee();
    TestSession testSession = new TestSession();
    service.openTest(testee, testSession, "testKey");
    verify(mockLegacyOpportunityService).openTest(testee, testSession, "testKey");

    verifyZeroInteractions(mockExamRepository);
  }

  @Test
  public void shouldNotExecuteLegacyCallIfNotEnabled() throws ReturnStatusException {
    Exam exam = new Exam.Builder()
      .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.IN_PROGRESS), Instant.now())
      .build();
    Response<Exam> response = new Response<>(exam);

    when(mockExamRepository.openExam(isA(OpenExamRequest.class))).thenReturn(response);

    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false,
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);

    Testee testee = new Testee();
    TestSession testSession = new TestSession();
    service.openTest(testee, testSession, "testKey");
    verify(mockExamRepository).openExam(isA(OpenExamRequest.class));

    verifyZeroInteractions(mockLegacyOpportunityService);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowIfBothImplementationsAreDisabled() {
    new RemoteOpportunityService(mockLegacyOpportunityService, false, false,
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
  }

  @Test
  public void shouldGetApprovedStatusNoErrors() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false,
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamApproval examApproval = new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), null);

    when(mockExamRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey())).thenReturn(
      new Response<>(examApproval));
    OpportunityStatus retStatus = service.getStatus(oppInstance);
    assertThat(retStatus.getStatus().toString()).isEqualTo("Approved");
    verify(mockExamRepository).getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());
  }

  @Test
  public void shouldGetDeniedStatusNoErrors() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false,
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamApproval examApproval = new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_DENIED, ExamStatusStage.OPEN), null);

    when(mockExamRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey())).thenReturn(
      new Response<>(examApproval));
    OpportunityStatus retStatus = service.getStatus(oppInstance);
    assertThat(retStatus.getStatus().toString()).isEqualTo("Denied");
    verify(mockExamRepository).getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowWithErrorsPresent() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, 
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamApproval examApproval = new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_DENIED, ExamStatusStage.OPEN), null);

    when(mockExamRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey())).thenReturn(
      new Response<>(examApproval, new ValidationError("Some Error", "ErrorCode")));
    service.getStatus(oppInstance);
  }

  @Test
  public void shouldReturnApprovalInfo() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, 
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamApproval examApproval = new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), null);

    when(mockExamRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey())).thenReturn(
      new Response<>(examApproval));
    ApprovalInfo approvalInfo = service.checkTestApproval(oppInstance);
    assertThat(approvalInfo.getStatus().toString()).isEqualTo("Approved");
    verify(mockExamRepository).getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());
    assertThat(approvalInfo.getComment()).isEqualTo(examApproval.getStatusChangeReason());
  }

  @Test
  public void shouldDenyApproval() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, 
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);

    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

    //mock getStatus
    when(mockExamRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey()))
      .thenReturn(new Response<>(new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_APPROVED), "reason")));

    final String deniedStatus = ExamStatusCode.STATUS_DENIED;
    OpportunityStatusChange statusChange = new OpportunityStatusChange(OpportunityStatusType.Pending, true, deniedStatus);
    when(mockExamRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name().toLowerCase(), deniedStatus)).thenReturn(Optional.<ValidationError>absent());
    service.denyApproval(oppInstance);

    verify(mockExamRepository).getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());
    verify(mockExamRepository).updateStatus(oppInstance.getExamId(), statusChange.getStatus().name().toLowerCase(), deniedStatus);
  }

  @Test
  public void shouldNotSetStatusIfStatusIsPaused() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, 
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    OpportunityStatus currentStatus = new OpportunityStatus();
    currentStatus.setStatus(OpportunityStatusType.Paused);

    //mock getStatus
    when(mockExamRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey()))
      .thenReturn(new Response<>(new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_PAUSED), "reason")));

    service.denyApproval(oppInstance);

    verify(mockExamRepository).getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());
    verify(mockExamRepository, never()).updateStatus((UUID) any(), (String) any(), (String) any());
  }

  @Test
  public void shouldUpdateStatus() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, 
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);

    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    OpportunityStatus currentStatus = new OpportunityStatus();
    currentStatus.setStatus(OpportunityStatusType.Approved);

    final String deniedStatus = ExamStatusCode.STATUS_DENIED;
    OpportunityStatusChange statusChange = new OpportunityStatusChange(OpportunityStatusType.Pending, true, deniedStatus);
    when(mockExamRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name().toLowerCase(), deniedStatus)).thenReturn(Optional.<ValidationError>absent());
    boolean isApproved = service.setStatus(oppInstance, statusChange);
    assertThat(isApproved).isTrue();
    verify(mockExamRepository).updateStatus(oppInstance.getExamId(), statusChange.getStatus().name().toLowerCase(), deniedStatus);
  }

  @Test
  public void shouldReturnTrueForErrorWithFalseCheckStatus() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, 
        mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);

    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    OpportunityStatus currentStatus = new OpportunityStatus();
    currentStatus.setStatus(OpportunityStatusType.Approved);

    final String deniedStatus = ExamStatusCode.STATUS_DENIED;
    OpportunityStatusChange statusChange = new OpportunityStatusChange(OpportunityStatusType.Pending, false, deniedStatus);
    when(mockExamRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name(), deniedStatus))
      .thenReturn(Optional.of(new ValidationError("Error", "Code")));
    boolean isApproved = service.setStatus(oppInstance, statusChange);
    assertThat(isApproved).isTrue();
    verify(mockExamRepository).updateStatus(oppInstance.getExamId(), statusChange.getStatus().name().toLowerCase(), deniedStatus);
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowForReturnStatusFailed() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);

    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    OpportunityStatus currentStatus = new OpportunityStatus();
    currentStatus.setStatus(OpportunityStatusType.Approved);

    final String deniedStatus = ExamStatusCode.STATUS_DENIED;
    OpportunityStatusChange statusChange = new OpportunityStatusChange(OpportunityStatusType.Pending, true, deniedStatus);
    when(mockExamRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name().toLowerCase(), deniedStatus))
      .thenReturn(Optional.of(new ValidationError(ExamStatusCode.STATUS_FAILED, "There was an error!")));
    service.setStatus(oppInstance, statusChange);
  }

  @Test
  public void shouldReturnFalseForValidationErrorReturned() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);

    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    OpportunityStatus currentStatus = new OpportunityStatus();
    currentStatus.setStatus(OpportunityStatusType.Approved);

    final String deniedStatus = ExamStatusCode.STATUS_DENIED;
    OpportunityStatusChange statusChange = new OpportunityStatusChange(OpportunityStatusType.Pending, true, deniedStatus);
    when(mockExamRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name().toLowerCase(), deniedStatus))
      .thenReturn(Optional.of(new ValidationError("Another Error", "There was an error!")));
    boolean isApproved = service.setStatus(oppInstance, statusChange);
    assertThat(isApproved).isFalse();
    verify(mockExamRepository).updateStatus(oppInstance.getExamId(), statusChange.getStatus().name().toLowerCase(), deniedStatus);
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowForErrorPresent() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    final String assessmentKey = "assessmentKey";
    Response<ExamConfiguration> errorResponse = new Response<>(new ValidationError("uh", "oh!"));

    when(mockExamRepository.startExam(oppInstance.getExamId(), "")).thenReturn(errorResponse);
    service.startTest(oppInstance, assessmentKey, null);
  }

  @Test
  public void shouldStartExamAndReturnTestConfig() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
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

    when(mockExamRepository.startExam(oppInstance.getExamId(), "")).thenReturn(new Response<>(mockExamConfig));
    TestConfig testConfig = service.startTest(oppInstance, assessmentKey, null);
    verify(mockExamRepository).startExam(oppInstance.getExamId(), "");

    assertThat(testConfig).isNotNull();
    assertThat(testConfig.getStatus()).isEqualTo(ExamStatusMapper.parseExamStatus(ExamStatusCode.STATUS_STARTED));
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
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
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

    when(mockExamRepository.findExamSegments(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey()))
      .thenReturn(Arrays.asList(seg1, seg2));
    OpportunitySegment.OpportunitySegments opportunitySegments = service.getSegments(oppInstance, true);
    verify(mockExamRepository).findExamSegments(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());

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

    assertThat(oppSeg1).isNotNull();
    assertThat(oppSeg2).isNotNull();

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

  @Test
  public void shouldWaitForSegmentEntry() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    service.waitForSegment(oppInstance, 2, TestSegment.TestSegmentApproval.Entry);
    verify(mockExamRepository).waitForSegmentApproval(eq(oppInstance.getExamId()), segmentApprovalRequestArgumentCaptor.capture());
    SegmentApprovalRequest request = segmentApprovalRequestArgumentCaptor.getValue();

    assertThat(request.getSegmentPosition()).isEqualTo(2);
    assertThat(request.getBrowserId()).isEqualTo(oppInstance.getExamBrowserKey());
    assertThat(request.getSessionId()).isEqualTo(oppInstance.getSessionKey());
    assertThat(request.isEntryApproval()).isTrue();
  }

  @Test
  public void shouldCheckSegmentApproval() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamApproval examApproval = new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_APPROVED), "reason");

    when(mockExamRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey()))
      .thenReturn(new Response<>(examApproval));
    ApprovalInfo info = service.checkSegmentApproval(oppInstance);

    verify(mockExamRepository).updateStatus(oppInstance.getExamId(), ExamStatusCode.STATUS_STARTED, "segment");
    assertThat(info.getStatus().name()).isEqualTo("Approved");
  }

  @Test
  public void shouldFindAttemptNumberForExam() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    Exam exam = new Exam.Builder()
      .withId(UUID.randomUUID())
      .withAttempts(3)
      .build();
    when(mockExamRepository.getExamById(oppInstance.getExamId()))
      .thenReturn(exam);

    int attemptNumber = service.getAttemptNumber(oppInstance);
    verify(mockExamRepository).getExamById(oppInstance.getExamId());
    assertThat(attemptNumber).isEqualTo(exam.getAttempts());
  }

  @Test
  public void shouldCheckSegmentApprovalLegacyEnabled() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, true, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamApproval examApproval = new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_APPROVED), "reason");
    OpportunityStatus oppStatusFromLegacy = new OpportunityStatus();
    oppStatusFromLegacy.setStatus(OpportunityStatusType.Approved);

    when(mockLegacyOpportunityService.checkSegmentApproval(isA(OpportunityInstance.class))).thenReturn(new ApprovalInfo(oppStatusFromLegacy));
    when(mockExamRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey()))
        .thenReturn(new Response<>(examApproval));
    ApprovalInfo info = service.checkSegmentApproval(oppInstance);

    verify(mockLegacyOpportunityService).checkSegmentApproval(isA(OpportunityInstance.class));
    verify(mockExamRepository).updateStatus(oppInstance.getExamId(), ExamStatusCode.STATUS_STARTED, "segment");
    verify(mockLegacyOpportunityService, never()).checkTestApproval(isA(OpportunityInstance.class));
    assertThat(info.getStatus().name()).isEqualTo("Approved");
  }

  @Test
  public void shouldCheckSegmentApprovalOnlyLegacy() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, false, true, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    OpportunityStatus oppStatusFromLegacy = new OpportunityStatus();
    oppStatusFromLegacy.setStatus(OpportunityStatusType.Approved);

    when(mockLegacyOpportunityService.checkSegmentApproval(isA(OpportunityInstance.class))).thenReturn(new ApprovalInfo(oppStatusFromLegacy));
    ApprovalInfo info = service.checkSegmentApproval(oppInstance);

    verify(mockLegacyOpportunityService).checkSegmentApproval(isA(OpportunityInstance.class));
    verify(mockExamRepository, never()).updateStatus(oppInstance.getExamId(), ExamStatusCode.STATUS_STARTED, "segment");
    verify(mockLegacyOpportunityService, never()).checkTestApproval(isA(OpportunityInstance.class));
    assertThat(info.getStatus().name()).isEqualTo("Approved");
  }

  @Test
  public void shouldCheckSegmentApprovalNotApproved() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    ExamApproval examApproval = new ExamApproval(oppInstance.getExamId(), new ExamStatusCode(ExamStatusCode.STATUS_PENDING), "waiting");

    when(mockExamRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(), oppInstance.getExamBrowserKey()))
      .thenReturn(new Response<>(examApproval));
    ApprovalInfo info = service.checkSegmentApproval(oppInstance);

    verify(mockExamRepository, never()).updateStatus(oppInstance.getExamId(), ExamStatusCode.STATUS_STARTED, "segment");
    assertThat(info.getStatus().name()).isEqualTo("Waiting");
  }

  @Test
  public void shouldWaitForSegmentExit() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    service.waitForSegment(oppInstance, 2, TestSegment.TestSegmentApproval.Exit);
    verify(mockExamRepository).waitForSegmentApproval(eq(oppInstance.getExamId()), segmentApprovalRequestArgumentCaptor.capture());
    SegmentApprovalRequest request = segmentApprovalRequestArgumentCaptor.getValue();

    assertThat(request.getSegmentPosition()).isEqualTo(2);
    assertThat(request.getBrowserId()).isEqualTo(oppInstance.getExamBrowserKey());
    assertThat(request.getSessionId()).isEqualTo(oppInstance.getSessionKey());
    assertThat(request.isEntryApproval()).isFalse();
  }

  @Test
  public void shouldExitSegment() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    service.exitSegment(oppInstance, 2);
    verify(mockExamSegmentRepository).exitSegment(oppInstance.getExamId(), 2);
  }

  @Test
  public void shouldFindExamAssessmentMetadataEmptyBrowserInfo() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    Testee testee = new Testee();
    testee.setKey(2112);
    TestSession session = new TestSession();
    session.setKey(UUID.randomUUID());
    BrowserInfo browserInfo = new BrowserInfo();

    ExamAssessmentMetadata metadata = new ExamAssessmentMetadata.Builder()
        .withSubject("ELA")
        .withAssessmentKey("assessmentKey1")
        .withAssessmentId("assessmentId1")
        .withAssessmentLabel("label")
        .withAttempt(3)
        .withMaxAttempts(12)
        .withDeniedReason("Some reason")
        .withStatus(ExamStatusCode.STATUS_PENDING)
        .withGrade("7")
        .build();

    when(mockExamRepository.findExamAssessmentInfo(testee.getKey(), session.getKey(), "7")).thenReturn(Collections.singletonList(metadata));
    when(configRepository.getBrowserTestRules(metadata.getAssessmentId())).thenReturn(new ArrayList<BrowserRule>());
    List<TestSelection> testSelections = service.getEligibleTests(testee, session, "7", browserInfo);
    assertThat(testSelections).hasSize(1);
    TestSelection selection = testSelections.get(0);
    assertThat(selection.getTestKey()).isEqualTo(metadata.getAssessmentKey());
    assertThat(selection.getTestID()).isEqualTo(metadata.getAssessmentId());
    assertThat(selection.getOpportunity()).isEqualTo(metadata.getAttempt());
    assertThat(selection.getMode()).isEqualTo("online");
    assertThat(selection.getDisplayName()).isEqualTo(metadata.getAssessmentLabel());
    assertThat(selection.getMaxOpportunities()).isEqualTo(metadata.getMaxAttempts());
    assertThat(selection.getSubject()).isEqualTo(metadata.getSubject());
    assertThat(selection.getGrade()).isEqualTo(metadata.getGrade());
    assertThat(selection.getTestStatus()).isEqualTo(TestSelection.Status.Start);
    assertThat(selection.getReasonKey()).isNull();
  }

  @Test
  public void shouldFindExamAssessmentMetadataDeniedBrowserInfo() throws ReturnStatusException {
    service = new RemoteOpportunityService(mockLegacyOpportunityService, true, false, mockExamRepository, mockExamSegmentRepository, mockTestOpportunityExamMapDao, configRepository);
    Testee testee = new Testee();
    testee.setKey(2112);
    TestSession session = new TestSession();
    session.setKey(UUID.randomUUID());
    BrowserInfo browserInfo = new BrowserInfo();
    BrowserRule browserRule = new BrowserRule();
    browserRule.setAction(BrowserAction.Deny);

    ExamAssessmentMetadata metadata = new ExamAssessmentMetadata.Builder()
        .withSubject("ELA")
        .withAssessmentKey("assessmentKey1")
        .withAssessmentId("assessmentId1")
        .withAssessmentLabel("label")
        .withAttempt(3)
        .withMaxAttempts(12)
        .withDeniedReason("Some reason")
        .withStatus(ExamStatusCode.STATUS_PENDING)
        .withGrade("7")
        .build();

    when(mockExamRepository.findExamAssessmentInfo(testee.getKey(), session.getKey(), "7")).thenReturn(Collections.singletonList(metadata));
    when(configRepository.getBrowserTestRules(metadata.getAssessmentId())).thenReturn(Collections.singletonList(browserRule));
    List<TestSelection> testSelections = service.getEligibleTests(testee, session, "7", browserInfo);
    assertThat(testSelections).hasSize(1);
    TestSelection selection = testSelections.get(0);
    assertThat(selection.getTestKey()).isEqualTo(metadata.getAssessmentKey());
    assertThat(selection.getTestID()).isEqualTo(metadata.getAssessmentId());
    assertThat(selection.getOpportunity()).isEqualTo(metadata.getAttempt());
    assertThat(selection.getMode()).isEqualTo("online");
    assertThat(selection.getDisplayName()).isEqualTo(metadata.getAssessmentLabel());
    assertThat(selection.getMaxOpportunities()).isEqualTo(metadata.getMaxAttempts());
    assertThat(selection.getSubject()).isEqualTo(metadata.getSubject());
    assertThat(selection.getGrade()).isEqualTo(metadata.getGrade());
    assertThat(selection.getTestStatus()).isEqualTo(TestSelection.Status.Disabled);
    assertThat(selection.getReasonKey()).isEqualTo("BrowserDeniedTest");
  }
}








