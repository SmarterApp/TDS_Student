package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
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
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.sql.abstractions.ExamRepository;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
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
}