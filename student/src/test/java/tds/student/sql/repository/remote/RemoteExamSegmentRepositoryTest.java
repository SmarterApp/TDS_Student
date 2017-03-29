package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
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
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

import tds.student.sql.repository.remote.impl.RemoteExamSegmentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteExamSegmentRepositoryTest {
  private RemoteExamSegmentRepository remoteSegmentExamRepository;

  @Mock
  private RestTemplate mockRestTemplate;

  @Before
  public void setUp() {
    remoteSegmentExamRepository = new RemoteExamSegmentRepository(mockRestTemplate, "http://localhost:8080/exam");
  }

  @Test
  public void shouldExitSegment() throws Exception {
    final UUID examId = UUID.randomUUID();
    final int segmentPosition = 2;

    when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenReturn(new ResponseEntity(HttpStatus.NO_CONTENT));
    remoteSegmentExamRepository.exitSegment(examId, segmentPosition);
    verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class));
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowForBadResponseExitSegment() throws Exception {
    final UUID examId = UUID.randomUUID();
    final int segmentPosition = 2;

    when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid", null, null));
    remoteSegmentExamRepository.exitSegment(examId, segmentPosition);
  }

  @Test
  public void shouldCheckIfSegmentsAreCompleted() throws Exception {
    final UUID examId = UUID.randomUUID();
    when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenReturn(new ResponseEntity(true, HttpStatus.OK));
    assertThat(remoteSegmentExamRepository.checkSegmentsSatisfied(examId)).isTrue();
    verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class));
  }

  @Test (expected = ReturnStatusException.class)
  public void shouldThrowReturnStatusExceptionForRestClientErrorSegmentsCompleted() throws Exception {
    final UUID examId = UUID.randomUUID();
    when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid", null, null));
    remoteSegmentExamRepository.checkSegmentsSatisfied(examId);
  }
}
