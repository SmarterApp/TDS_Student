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

import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.data.ApprovalInfo;
import tds.student.sql.abstractions.ExamRepository;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityStatus;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.OpportunityStatusType;
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
}