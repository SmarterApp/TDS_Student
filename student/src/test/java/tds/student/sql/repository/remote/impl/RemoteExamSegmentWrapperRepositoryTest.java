/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.student.sql.repository.remote.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tds.exam.wrapper.ExamSegmentWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteExamSegmentWrapperRepositoryTest {
  @Mock
  private RestTemplate restTemplate;

  private RemoteExamSegmentWrapperRepository repository;

  @Before
  public void setUp() {
    repository = new RemoteExamSegmentWrapperRepository(restTemplate, "http://localhost:8080/exam");
  }

  @Test
  public void shouldFindExamSegmentWrappersForExam() throws URISyntaxException, ReturnStatusException {
    UUID examId = UUID.randomUUID();
    ExamSegmentWrapper wrapper = mock(ExamSegmentWrapper.class);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

    URI uri = new URI("http://localhost:8080/exam/" + examId + "/segmentWrappers");
    ResponseEntity<List<ExamSegmentWrapper>> entity = new ResponseEntity<>(Collections.singletonList(wrapper), HttpStatus.OK);

    when(restTemplate.exchange(
      uri,
      HttpMethod.GET,
      requestHttpEntity,
      new ParameterizedTypeReference<List<ExamSegmentWrapper>>() {})).thenReturn(entity);

    assertThat(repository.findAllExamSegmentWrappersForExam(examId)).containsExactly(wrapper);
  }

  @Test
  public void shouldFindExamSegmentWrappersForExamAndPage() throws URISyntaxException, ReturnStatusException {
    UUID examId = UUID.randomUUID();
    ExamSegmentWrapper wrapper = mock(ExamSegmentWrapper.class);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

    URI uri = new URI("http://localhost:8080/exam/" + examId + "/segmentWrappers?pagePosition=1");

    ResponseEntity<List<ExamSegmentWrapper>> entity = new ResponseEntity<>(Collections.singletonList(wrapper), HttpStatus.OK);

    when(restTemplate.exchange(
      uri,
      HttpMethod.GET,
      requestHttpEntity,
      new ParameterizedTypeReference<List<ExamSegmentWrapper>>() {})).thenReturn(entity);

    assertThat(repository.findExamSegmentWrappersForExamAndPagePosition(examId,1).get()).isEqualTo(wrapper);
  }

  @Test
  public void shouldReturnEmptyIfCannotFindExamSegmentWrappersForExamAndPage() throws URISyntaxException, ReturnStatusException {
    UUID examId = UUID.randomUUID();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

    URI uri = new URI("http://localhost:8080/exam/" + examId + "/segmentWrappers?pagePosition=1");

    ResponseEntity<List<ExamSegmentWrapper>> entity = new ResponseEntity<>(Collections.<ExamSegmentWrapper>emptyList(), HttpStatus.OK);

    when(restTemplate.exchange(
      uri,
      HttpMethod.GET,
      requestHttpEntity,
      new ParameterizedTypeReference<List<ExamSegmentWrapper>>() {})).thenReturn(entity);

    assertThat(repository.findExamSegmentWrappersForExamAndPagePosition(examId,1).isPresent()).isFalse();
  }
}