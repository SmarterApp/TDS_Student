package tds.student.sql.repository;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamApproval;
import tds.exam.ExamConfiguration;
import tds.exam.ExamPrintRequest;
import tds.exam.ExamSegment;
import tds.exam.OpenExamRequest;
import tds.exam.SegmentApprovalRequest;

@Repository
public class RemoteExamRepository implements ExamRepository {
  private final RestTemplate restTemplate;
  private final String examUrl;
  private final ObjectMapper objectMapper;

  @Autowired
  public RemoteExamRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                              @Value("${tds.exam.remote.url}") final String examUrl,
                              @Qualifier("integrationObjectMapper") final ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.examUrl = examUrl;
    this.objectMapper = objectMapper;
  }

  @Override
  public Response<Exam> openExam(final OpenExamRequest openExamRequest) throws ReturnStatusException {
    HttpEntity<OpenExamRequest> requestHttpEntity = new HttpEntity<>(openExamRequest);
    Response<Exam> response;

    try {
      ResponseEntity<Response<Exam>> responseEntity = restTemplate.exchange(
        examUrl,
        HttpMethod.POST,
        requestHttpEntity,
        new ParameterizedTypeReference<Response<Exam>>() {
        });

      response = responseEntity.getBody();
    } catch (HttpClientErrorException hce) {
      if (isClientError(hce.getStatusCode())) {
        response = handleErrorResponse(hce.getResponseBodyAsString());
      } else {
        throw new ReturnStatusException(hce);
      }
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }

    return response;
  }

  private static boolean isClientError(HttpStatus status) {
    return HttpStatus.Series.CLIENT_ERROR.equals(status.series());
  }

  private Response<Exam> handleErrorResponse(String body) throws ReturnStatusException {
    try {
      JavaType type = objectMapper.getTypeFactory().constructParametricType(Response.class, Exam.class);
      return objectMapper.readValue(body, type);
    } catch (IOException e) {
      throw new ReturnStatusException(e);
    }
  }

  private NoContentResponseResource handleErrorResponseNoContent(String body) throws ReturnStatusException {
    try {
      JavaType type = objectMapper.getTypeFactory().constructType(NoContentResponseResource.class);
      return objectMapper.readValue(body, type);
    } catch (IOException e) {
      throw new ReturnStatusException(e);
    }
  }

  private Response<ExamConfiguration> handleErrorResponseExamConfig(String body) throws ReturnStatusException {
    try {
      JavaType type = objectMapper.getTypeFactory().constructParametricType(Response.class, Exam.class);
      return objectMapper.readValue(body, type);
    } catch (IOException e) {
      throw new ReturnStatusException(e);
    }
  }

  @Override
  public Response<ExamApproval> getApproval(final UUID examId, final UUID sessionId, final UUID browserId) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
    ResponseEntity<Response<ExamApproval>> responseEntity;

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/approval", examUrl, examId))
      .queryParam("sessionId", sessionId)
      .queryParam("browserId", browserId);

    try {
      responseEntity = restTemplate.exchange(
        builder.build().encode().toUri(),
        HttpMethod.GET,
        requestHttpEntity,
        new ParameterizedTypeReference<Response<ExamApproval>>() {
        });
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }

    return responseEntity.getBody();
  }

  @Override
  public List<ExamAccommodation> findApprovedAccommodations(UUID examId) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
    ResponseEntity<List<ExamAccommodation>> responseEntity;

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/accommodations/approved", examUrl, examId));

    try {
      responseEntity = restTemplate.exchange(
        builder.build().encode().toUri(),
        HttpMethod.GET,
        requestHttpEntity,
        new ParameterizedTypeReference<List<ExamAccommodation>>() {
        });
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }

    return responseEntity.getBody();
  }

  @Override
  public void approveAccommodations(UUID examId, ApproveAccommodationsRequest approveAccommodationsRequest) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(approveAccommodationsRequest, headers);

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/accommodations", examUrl, examId));

    try {
      restTemplate.exchange(
        builder.build().toUri(),
        HttpMethod.POST,
        requestHttpEntity,
        new ParameterizedTypeReference<String>() {
        });
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }
  }

  @Override
  public Optional<ValidationError> updateStatus(final UUID examId, final String status, final String reason) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/status", examUrl, examId))
      .queryParam("status", status)
      .queryParam("reason", reason);

    try {
      restTemplate.exchange(
        builder.build().toUri(),
        HttpMethod.PUT,
        requestHttpEntity,
        new ParameterizedTypeReference<NoContentResponseResource>() {
        });
    } catch (HttpClientErrorException hce) {
      // No need to throw a ReturnStatusException if its a 4xx here - we'll leave it up to the service calling this method
      if (isClientError(hce.getStatusCode())) {
        NoContentResponseResource responseResource = handleErrorResponseNoContent(hce.getResponseBodyAsString());
        if (responseResource.getErrors().length > 0) {
          return Optional.of(responseResource.getErrors()[0]);
        } else {
          throw new ReturnStatusException(hce);
        }
      } else {
        throw new ReturnStatusException(hce);
      }
    }

    return Optional.absent();
  }

  @Override
  public Response<ExamConfiguration> startExam(UUID examId) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
    Response<ExamConfiguration> response;
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/start", examUrl, examId));

    try {
      ResponseEntity<Response<ExamConfiguration>> responseEntity = restTemplate.exchange(
        builder.build().toUri(),
        HttpMethod.PUT,
        requestHttpEntity,
        new ParameterizedTypeReference<Response<ExamConfiguration>>() {
        });

      response = responseEntity.getBody();
    } catch (HttpClientErrorException hce) {
      if (isClientError(hce.getStatusCode())) {
        response = handleErrorResponseExamConfig(hce.getResponseBodyAsString());
      } else {
        throw new ReturnStatusException(hce);
      }
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }

    return response;
  }

  @Override
  public Response<List<ExamSegment>> findExamSegments(UUID examId, UUID sessionId, UUID browserId) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
    ResponseEntity<Response<List<ExamSegment>>> responseEntity;

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/segments/%s", examUrl, examId))
      .queryParam("sessionId", sessionId)
      .queryParam("browserId", browserId);

    try {
      responseEntity = restTemplate.exchange(
        builder.build().encode().toUri(),
        HttpMethod.GET,
        requestHttpEntity,
        new ParameterizedTypeReference<Response<List<ExamSegment>>>() {
        });
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }

    return responseEntity.getBody();
  }

  @Override
  public void createPrintRequest(ExamPrintRequest examPrintRequest) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(examPrintRequest, headers);

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/print", examUrl));

    try {
      restTemplate.exchange(
        builder.build().toUri(),
        HttpMethod.POST,
        requestHttpEntity,
        new ParameterizedTypeReference<String>() {
        });
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }
  }

  @Override
  public void waitForSegmentApproval(final UUID examId, final SegmentApprovalRequest segmentApprovalRequest) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(segmentApprovalRequest, headers);

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/segmentApproval", examUrl, examId));

    try {
      restTemplate.exchange(
        builder.build().toUri(),
        HttpMethod.PUT,
        requestHttpEntity,
        new ParameterizedTypeReference<NoContentResponseResource>() {
        });
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }
  }

  @Override
  public void exitSegment(final UUID examId, final int segmentPosition) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/exit/%s", examUrl, examId, segmentPosition));

    try {
      restTemplate.exchange(
        builder.build().toUri(),
        HttpMethod.PUT,
        requestHttpEntity,
        new ParameterizedTypeReference<NoContentResponseResource>() {
        });
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }
  }
}
