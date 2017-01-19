package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.sql.abstractions.ExamRepository;
import tds.student.sql.data.OpportunityInfo;
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
  private RestTemplate restTemplate;

  @Mock
  private IOpportunityService legacyOpportunityService;

  private IOpportunityService service;

  @Mock
  private ExamRepository examRepository;

  @Before
  public void setUp() {
    service = new RemoteOpportunityService(legacyOpportunityService, true, examRepository);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void shouldReturnTestOpportunityBasedOnExam() throws ReturnStatusException {
    ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
    ArgumentCaptor<HttpMethod> methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

    Exam exam = new Exam.Builder()
      .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.IN_PROGRESS), Instant.now())
      .build();
    Response<Exam> response = new Response<>(exam);

    when(examRepository.openExam(isA(OpenExamRequest.class))).thenReturn(response);

    TestSession session = new TestSession();
    Testee testee = new Testee();

    OpportunityInfo info = service.openTest(testee, session, "testKey");
    verify(legacyOpportunityService).openTest(testee, session, "testKey");

    assertThat(info).isNotNull();
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowExceptionWhenErrorsArePresent() throws ReturnStatusException {
    ValidationError error = new ValidationError("TEST", "TEST");

    Response<Exam> response = new Response<>(error);
    when(examRepository.openExam(isA(OpenExamRequest.class))).thenReturn(response);

    TestSession session = new TestSession();
    Testee testee = new Testee();

    OpportunityInfo info = service.openTest(testee, session, "testKey");
    verify(legacyOpportunityService).openTest(testee, session, "testKey");
  }

  @Test
  public void shouldNotExecuteIfNotEnabled() throws ReturnStatusException {
    service = new RemoteOpportunityService(legacyOpportunityService, false, examRepository);

    Testee testee = new Testee();
    TestSession testSession = new TestSession();
    service.openTest(testee, testSession, "testKey");
    verify(legacyOpportunityService).openTest(testee, testSession, "testKey");

    verifyZeroInteractions(restTemplate);
  }
}