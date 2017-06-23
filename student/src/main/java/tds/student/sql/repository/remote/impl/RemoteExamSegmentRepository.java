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
import tds.student.sql.repository.remote.ExamSegmentRepository;

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

  @Override
  public boolean checkSegmentsSatisfied(final UUID examId) throws ReturnStatusException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/segments/completed", examUrl, examId));
    Boolean completed;

    try {
      completed = restTemplate.exchange(
        builder.build().toUri(),
        HttpMethod.GET,
        requestHttpEntity,
        new ParameterizedTypeReference<Boolean>() {
        }).getBody();
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }

    return completed;
  }
}
