package tds.student.sql.repository;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import java.net.URI;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamConfiguration;
import tds.exam.ExamStatusCode;
import tds.exam.OpenExamRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteExamRepositoryTest {
  private RemoteExamRepository remoteExamRepository;

  @Mock
  private RestTemplate mockRestTemplate;

  private static final String ERROR_JSON = "{\n" +
    "  \"error\": {\n" +
    "    \"code\": \"sessionNotOpen\",\n" +
    "    \"message\": \"Session 549ed8e0-d1cd-4830-8ae5-c35e92abb282 is not open\"\n" +
    "  }\n" +
    "}";
  
  private static final String NO_CONTENT_JSON = "{\n" +
    "  \"errors\": [{\n" +
    "    \"code\": \"sessionNotOpen\",\n" +
    "    \"message\": \"Session 549ed8e0-d1cd-4830-8ae5-c35e92abb282 is not open\"\n" +
    "  }]\n" +
    "}";

  @Before
  public void setUp() {
    ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new GuavaModule())
      .registerModule(new JodaModule());

    remoteExamRepository = new RemoteExamRepository(mockRestTemplate, "http://localhost:8080/exam", objectMapper);
  }

  @Test
  public void shouldReturnResponseOnSuccess() throws ReturnStatusException {
    Exam exam = new Exam.Builder().withId(UUID.randomUUID()).build();
    Response<Exam> response = new Response<>(exam);
    ResponseEntity<Response<Exam>> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
    OpenExamRequest openExamRequest = new OpenExamRequest.Builder().build();

    when(mockRestTemplate.exchange(isA(String.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenReturn(responseEntity);

    assertThat(remoteExamRepository.openExam(openExamRequest)).isEqualTo(response);
  }

  @Test (expected = ReturnStatusException.class)
  public void shouldThrowReturnStatusExceptionWhenRestClientUnhandledExceptionIsThrown() throws ReturnStatusException {
    when(mockRestTemplate.exchange(isA(String.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenThrow(new RestClientException("Fail"));
    remoteExamRepository.openExam(new OpenExamRequest.Builder().build());
  }

  @Test (expected = ReturnStatusException.class)
  public void shouldThrowReturnStatusExceptionWhenNonClientHttpClientExceptionIsThrown() throws ReturnStatusException {
    when(mockRestTemplate.exchange(isA(String.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    remoteExamRepository.openExam(new OpenExamRequest.Builder().build());
  }

  @Test
  public void shouldReturnResponseOnClientError() throws ReturnStatusException {
    OpenExamRequest openExamRequest = new OpenExamRequest.Builder().build();

    when(mockRestTemplate.exchange(isA(String.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid", ERROR_JSON.getBytes(), null));

    Response<Exam> result = remoteExamRepository.openExam(openExamRequest);

    assertThat(result.getData().isPresent()).isFalse();
    assertThat(result.getError().isPresent()).isTrue();

    ValidationError error = result.getError().get();

    assertThat(error.getCode()).isEqualTo("sessionNotOpen");
    assertThat(error.getMessage()).isEqualTo("Session 549ed8e0-d1cd-4830-8ae5-c35e92abb282 is not open");
  }
  
  @Test
  public void shouldUpdateStatusNoError() throws ReturnStatusException {
    final UUID examId = UUID.randomUUID();
    final String status = ExamStatusCode.STATUS_PENDING;
    Optional<ValidationError> maybeError = remoteExamRepository.updateStatus(examId, status, "Some reason");
    verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class));
    assertThat(maybeError.isPresent()).isFalse();
  }
  
  @Test
  public void shouldReturnValidationError() throws ReturnStatusException {
    final UUID examId = UUID.randomUUID();
    final String status = ExamStatusCode.STATUS_PENDING;
    when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid", NO_CONTENT_JSON.getBytes(), null));
    Optional<ValidationError> maybeError = remoteExamRepository.updateStatus(examId, status, "Some reason");
    verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class));
    assertThat(maybeError.isPresent()).isTrue();
  }
  
  @Test(expected = ReturnStatusException.class)
  public void shouldThrowForInternalServerError() throws ReturnStatusException {
    final UUID examId = UUID.randomUUID();
    final String status = ExamStatusCode.STATUS_PENDING;
    when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid", NO_CONTENT_JSON.getBytes(), null));
    Optional<ValidationError> maybeError = remoteExamRepository.updateStatus(examId, status, "Some reason");
    verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class));
    assertThat(maybeError.isPresent()).isTrue();
  }
  
  @Test
  public void shouldReturnExamConfiguration() throws ReturnStatusException {
    UUID examId = UUID.randomUUID();
    Response<ExamConfiguration> mockResponse = new Response<>(
      new ExamConfiguration.Builder()
        .withStatus("test")
        .build());
    
    when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenReturn(new ResponseEntity(mockResponse, HttpStatus.OK));
  
    Response<ExamConfiguration> response = remoteExamRepository.startExam(examId);
    assertThat(response.getData().isPresent()).isTrue();
    assertThat(response.getData().get().getStatus()).isEqualTo("test");
  }
  
  @Test
  public void shouldReturnResponseWithValidationErrorsForClientException() throws ReturnStatusException {
    UUID examId = UUID.randomUUID();
    Response<ExamConfiguration> mockResponse = new Response<>(
      new ExamConfiguration.Builder()
        .withStatus("test")
        .build());
  
    when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid", ERROR_JSON.getBytes(), null));
  
    Response<ExamConfiguration> response = remoteExamRepository.startExam(examId);
    
    assertThat(response.getError().isPresent()).isTrue();
  }
  
  @Test(expected = ReturnStatusException.class)
  public void shouldThrowForInternalServerErrorStartExam() throws ReturnStatusException {
    UUID examId = UUID.randomUUID();
    when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid", ERROR_JSON.getBytes(), null));
    remoteExamRepository.startExam(examId);
  }
}