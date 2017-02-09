package tds.student.sql.repository;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

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

  @Autowired
  public RemoteExamRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                              @Value("${tds.exam.remote.url}") final String examUrl) {
    this.restTemplate = restTemplate;
    this.examUrl = examUrl;
  }

  @Override
  public Response<Exam> openExam(final OpenExamRequest openExamRequest) throws ReturnStatusException {
    HttpEntity<OpenExamRequest> requestHttpEntity = new HttpEntity<>(openExamRequest);
    ResponseEntity<Response<Exam>> responseEntity;

    try {
      responseEntity = restTemplate.exchange(
        examUrl,
        HttpMethod.POST,
        requestHttpEntity,
        new ParameterizedTypeReference<Response<Exam>>() {
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
}
