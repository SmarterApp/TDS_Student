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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.repository.ContentRepository;

@Repository
public class RemoteContentRepository implements ContentRepository {
    private final RestTemplate restTemplate;
    private final String contentUrl;

    @Autowired
    public RemoteContentRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                                      @Value("${tds.content.remote.url}") final String contentUrl) {
        this.restTemplate = restTemplate;
        this.contentUrl = contentUrl;
    }

    @Override
    public ITSDocument findItemDocument(final String itemPath, final AccLookup accommodations) throws ReturnStatusException {
        if (StringUtils.isEmpty (itemPath)) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(accommodations, headers);
        ResponseEntity<ITSDocument> responseEntity;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/item", contentUrl))
            .queryParam("itemPath", itemPath);

        try {
            responseEntity = restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.POST,
                requestHttpEntity,
                new ParameterizedTypeReference<ITSDocument>() {
                });
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }

        return responseEntity.getBody();
    }

    @Override
    public byte[] findResource(final String resourcePath) {
        HttpHeaders headers = new HttpHeaders();
//        headers.set("Accept", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
        ResponseEntity<Resource> responseEntity;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/resource", contentUrl))
            .queryParam("resourcePath", resourcePath);

        responseEntity = restTemplate.exchange(
            builder.build().toUri(),
            HttpMethod.GET,
            requestHttpEntity,
            new ParameterizedTypeReference<Resource>() {
            });

        return ((ByteArrayResource)responseEntity.getBody()).getByteArray();
    }
}
