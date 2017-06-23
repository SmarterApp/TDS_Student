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

import com.google.common.net.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteItemScoringRepositoryTest {

    private final static String EXAM_URL = "http://exam/exam";

    @Mock
    private RestTemplate mockRestTemplate;

    private RemoteItemScoringRepository repository;

    @Before
    public void setup() {
        repository = new RemoteItemScoringRepository(mockRestTemplate, EXAM_URL);
    }

    @Test
    public void itShouldUpdateResponses() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final String clientName = "clientName";
        final Float pageDuration = 1.23f;
        final List<ItemResponseUpdate> responseUpdates = newArrayList(mock(ItemResponseUpdate.class));
        final List<ItemResponseUpdateStatus> statuses = newArrayList(mock(ItemResponseUpdateStatus.class));

        final ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        final ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(mockRestTemplate.exchange(
            uriCaptor.capture(),
            eq(HttpMethod.PUT),
            requestCaptor.capture(),
            any(ParameterizedTypeReference.class)))
            .thenReturn(new ResponseEntity(statuses, HttpStatus.OK));

        assertThat(repository.updateResponses(examId, sessionId, browserId, clientName, pageDuration, responseUpdates))
            .containsOnlyElementsOf(statuses);

        final URI uri = uriCaptor.getValue();
        List<NameValuePair> queryParams = URLEncodedUtils.parse(uri, "UTF-8");
        assertThat(uri.getHost()).isEqualTo("exam");
        assertThat(queryParams)
            .contains(new BasicNameValuePair("sessionId", sessionId.toString()))
            .contains(new BasicNameValuePair("browserId", browserId.toString()))
            .contains(new BasicNameValuePair("clientName", clientName))
            .contains(new BasicNameValuePair("pageDuration", pageDuration.toString()));
        assertThat(uri.getPath()).isEqualTo("/exam/" + examId.toString() + "/scores/responses");

        final HttpEntity<List<ItemResponseUpdate>> request = requestCaptor.getValue();
        assertThat(request.getBody()).containsOnlyElementsOf(responseUpdates);
        assertThat(request.getHeaders()).contains(entry(HttpHeaders.ACCEPT, of(MediaType.APPLICATION_JSON_VALUE)));
    }

}