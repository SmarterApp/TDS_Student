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

import tds.accommodation.Accommodation;
import tds.assessment.Assessment;

@Repository
public class RemoteAssessmentRepository implements AssessmentRepository {
  private final RestTemplate restTemplate;
  private final String assessmentUrl;

  @Autowired
  public RemoteAssessmentRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                              @Value("${tds.assessment.remote.url}") final String assessmentUrl) {
    this.restTemplate = restTemplate;
    this.assessmentUrl = assessmentUrl;
  }

  @Override
  public Assessment findAssessment(final String clientName, final String key) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
    ResponseEntity<Assessment> responseEntity;

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/assessments/%s", assessmentUrl, clientName, key));

    try {
      responseEntity = restTemplate.exchange(
        builder.build().toUri(),
        HttpMethod.GET,
        requestHttpEntity,
        new ParameterizedTypeReference<Assessment>() {
        });
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }

    return responseEntity.getBody();
  }

  @Override
  public List<Accommodation> findAccommodations(final String clientName, final String assessmentKey) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
    ResponseEntity<List<Accommodation>> responseEntity;

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/assessments/accommodations", assessmentUrl, clientName))
      .queryParam("assessmentKey", assessmentKey);

    try {
      responseEntity = restTemplate.exchange(
        builder.build().toUri(),
        HttpMethod.GET,
        requestHttpEntity,
        new ParameterizedTypeReference<List<Accommodation>>() {
        });
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }

    return responseEntity.getBody();
  }
}
