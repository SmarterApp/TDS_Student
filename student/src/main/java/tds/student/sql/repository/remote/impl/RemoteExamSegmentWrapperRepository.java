package tds.student.sql.repository.remote.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.exam.ExamAssessmentMetadata;
import tds.exam.wrapper.ExamSegmentWrapper;
import tds.student.sql.repository.remote.ExamSegmentWrapperRepository;

@Repository
public class RemoteExamSegmentWrapperRepository implements ExamSegmentWrapperRepository {
  private final RestTemplate restTemplate;
  private final String examUrl;

  @Autowired
  public RemoteExamSegmentWrapperRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                                            @Value("${tds.exam.remote.url}") final String examUrl) {
    this.restTemplate = restTemplate;
    this.examUrl = examUrl;
  }

  @Override
  public List<ExamSegmentWrapper> findAllExamSegmentWrappersForExam(final UUID examId) throws ReturnStatusException {
    return find(examId, null);
  }

  @Override
  public Optional<ExamSegmentWrapper> findExamSegmentWrappersForExamAndPagePosition(final UUID examId, final int pagePosition) throws ReturnStatusException {
    List<ExamSegmentWrapper> wrappers = find(examId, pagePosition);

    if (wrappers.isEmpty()) {
      return Optional.absent();
    }

    //There should only be a single result if the data is correctly being persisted and created
    return Optional.of(wrappers.get(0));
  }

  private List<ExamSegmentWrapper> find(final UUID examId, final Integer pagePosition) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
    ResponseEntity<Response<List<ExamAssessmentMetadata>>> responseEntity;

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(examUrl)
      .pathSegment("{examId}", "segmentWrappers");

    if (pagePosition != null) {
      builder.queryParam("pagePosition", 1);
    }

    try {
      return restTemplate.exchange(
        builder.buildAndExpand(examId).toUri(),
        HttpMethod.GET,
        requestHttpEntity,
        new ParameterizedTypeReference<List<ExamSegmentWrapper>>() {
        }).getBody();
    } catch (final HttpStatusCodeException e) {
      final ReturnStatusException statusException = new ReturnStatusException("Failed to get exam segment wrappers: " + e.getResponseBodyAsString());
      statusException.getReturnStatus().setHttpStatusCode(500);
      throw statusException;
    } catch (final RestClientException rce) {
      throw new ReturnStatusException(rce);
    }
  }
}
