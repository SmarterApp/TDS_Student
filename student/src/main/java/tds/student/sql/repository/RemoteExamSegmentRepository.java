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
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import tds.common.web.resources.NoContentResponseResource;

@Repository
public class RemoteExamSegmentRepository implements ExamSegmentRepository {

  private final RestTemplate restTemplate;
  private final String examUrl;

  @Autowired
  public RemoteExamSegmentRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                                     @Value("${tds.exam.remote.url}") final String examUrl) {
    this.restTemplate = restTemplate;
    this.examUrl = examUrl;
  }
  
  @Override
  public void exitSegment(final UUID examId, final int segmentPosition) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/segments/%d/exit", examUrl, examId, segmentPosition));

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
