package tds.student.sql.repository;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

import java.io.IOException;

import tds.common.Response;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamApproval;
import tds.exam.OpenExamRequest;
import tds.student.sql.abstractions.ExamRepository;

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
      if(isClientError(hce.getStatusCode())) {
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
}
