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
import com.rabbitmq.client.AMQP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;
import tds.student.sql.repository.remote.ItemScoringRepository;

import java.util.List;
import java.util.UUID;

/**
 * This implementation of an ItemScoringRepository forwards item responses
 * to a remote exam service.
 */
@Repository
public class RemoteItemScoringRepository implements ItemScoringRepository {

    private final RestTemplate restTemplate;
    private final String examUrl;

    @Autowired
    public RemoteItemScoringRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                                       @Value("${tds.exam.remote.url}") final String examUrl) {
        this.restTemplate = restTemplate;
        this.examUrl = examUrl;
    }

    @Override
    public List<ItemResponseUpdateStatus> updateResponses(final UUID examId,
                                                          final UUID sessionId,
                                                          final UUID browserId,
                                                          final String clientName,
                                                          final Float pageDuration,
                                                          final List<ItemResponseUpdate> responseUpdates) throws ReturnStatusException {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<List<ItemResponseUpdate>> requestHttpEntity = new HttpEntity<>(responseUpdates, headers);

        final UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(String.format("%s/%s/scores/responses", examUrl, examId))
            .queryParam("sessionId", sessionId)
            .queryParam("browserId", browserId)
            .queryParam("clientName", clientName)
            .queryParam("pageDuration", pageDuration);

        try {
            return restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.PUT,
                requestHttpEntity,
                new ParameterizedTypeReference<List<ItemResponseUpdateStatus>>() {
                }).getBody();
        } catch (final HttpStatusCodeException e) {
            final ReturnStatusException statusException = new ReturnStatusException("Failed to update response: " + e.getResponseBodyAsString());
            statusException.getReturnStatus().setHttpStatusCode(500);
            throw statusException;
        } catch (final RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }
}
