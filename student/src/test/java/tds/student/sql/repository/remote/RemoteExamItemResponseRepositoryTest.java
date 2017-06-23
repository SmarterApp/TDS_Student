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

package tds.student.sql.repository.remote;

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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tds.common.web.resources.NoContentResponseResource;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.repository.remote.impl.RemoteExamItemResponseRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteExamItemResponseRepositoryTest {
    private RemoteExamItemResponseRepository remoteExamItemResponseRepository;

    @Mock
    private RestTemplate mockRestTemplate;

    @Before
    public void setUp() {
        remoteExamItemResponseRepository = new RemoteExamItemResponseRepository(mockRestTemplate, "http://localhost:8080/exam");
    }

    @Test
    public void shouldMarkForReviewSuccessful() throws ReturnStatusException {
        final OpportunityInstance opportunityInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        final int position = 3;
        final boolean mark = true;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(mark, headers);

        URI uri = UriComponentsBuilder.fromHttpUrl(String.format("http://localhost:8080/exam/%s/item/%d/review",
          opportunityInstance.getExamId(), 3))
          .queryParam("sessionId", opportunityInstance.getSessionKey())
          .queryParam("browserId", opportunityInstance.getExamBrowserKey())
          .build()
          .toUri();

        remoteExamItemResponseRepository.markItemForReview(opportunityInstance, position, mark);

        verify(mockRestTemplate).exchange(
          uri,
          HttpMethod.PUT,
          requestHttpEntity,
          new ParameterizedTypeReference<NoContentResponseResource>() {
          });
    }

    @Test
    public void shouldGetNextItemGroup() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();

        URI uri = UriComponentsBuilder
          .fromHttpUrl(String.format("http://localhost:8080/exam/%s/item", examId))
          .queryParam("lastPagePosition", 1)
          .queryParam("lastItemPosition", 1)
          .build()
          .toUri();

        OpportunityItem item = new OpportunityItem();
        ResponseEntity<List<OpportunityItem>> response = new ResponseEntity<>(Collections.singletonList(item), HttpStatus.OK);

        when(mockRestTemplate.exchange(
          uri,
          HttpMethod.POST,
          null,
          new ParameterizedTypeReference<List<OpportunityItem>>() {
          })).thenReturn(response);

        assertThat(remoteExamItemResponseRepository.createNextItemGroup(examId, 1, 1)).containsExactly(item);

        verify(mockRestTemplate).exchange(
          uri,
          HttpMethod.POST,
          null,
          new ParameterizedTypeReference<List<OpportunityItem>>() {
          });
    }

    @Test (expected = ReturnStatusException.class)
    public void shouldThrowIfFailureToCreateNextItemGroup() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();

        URI uri = UriComponentsBuilder
          .fromHttpUrl(String.format("http://localhost:8080/exam/%s/item", examId))
          .queryParam("lastPagePosition", 1)
          .queryParam("lastItemPosition", 1)
          .build()
          .toUri();

        OpportunityItem item = new OpportunityItem();

        when(mockRestTemplate.exchange(
          uri,
          HttpMethod.POST,
          null,
          new ParameterizedTypeReference<List<OpportunityItem>>() {
          })).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        remoteExamItemResponseRepository.createNextItemGroup(examId, 1, 1);
    }
}
