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
import org.springframework.cache.annotation.Cacheable;
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

import tds.assessment.Assessment;
import tds.dll.common.performance.caching.CacheType;
import tds.student.sql.repository.remote.AssessmentRepository;

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
  @Cacheable(CacheType.LongTerm)
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
}
