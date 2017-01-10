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
import tds.student.services.abstractions.IOpportunityService;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteOpportunityServiceTest {
  private static final String examUrl = "http://server:8080/exam";

  @Mock
  private RestTemplate restTemplate;

  private IOpportunityService service;

  @Before
  public void setUp() {
    service = new RemoteOpportunityService(restTemplate, examUrl, true);
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
    ResponseEntity<Response<Exam>> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
    when(restTemplate.exchange(
      urlCaptor.capture(),
      methodCaptor.capture(), entityCaptor.capture(), isA(ParameterizedTypeReference.class))).thenReturn(responseEntity);

    TestSession session = new TestSession();
    Testee testee = new Testee();

    OpportunityInfo info = service.openTest(testee, session, "testKey");

    assertThat(info).isNotNull();
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowExceptionWhenErrorsArePresent() throws ReturnStatusException {
    ValidationError error = new ValidationError("TEST", "TEST");

    Response<Exam> response = new Response<Exam>(error);
    ResponseEntity<Response<Exam>> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
    when(restTemplate.exchange(
      isA(String.class),
      isA(HttpMethod.class),
      isA(HttpEntity.class),
      isA(ParameterizedTypeReference.class))).thenReturn(responseEntity);

    TestSession session = new TestSession();
    Testee testee = new Testee();

    OpportunityInfo info = service.openTest(testee, session, "testKey");
  }

  @Test
  public void shouldNotExecuteIfNotEnabled() throws ReturnStatusException {
    service = new RemoteOpportunityService(restTemplate, examUrl, false);

    service.openTest(new Testee(), new TestSession(), "");

    verifyZeroInteractions(restTemplate);
  }
}