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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Type;
import java.util.List;

import tds.common.Response;
import tds.exam.ExamApproval;
import tds.exam.ExamPage;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.ExamPageRepository;

import static com.google.common.collect.Lists.newArrayList;

@Repository
public class RemoteExamPageRepository implements ExamPageRepository {
    private final RestTemplate restTemplate;
    private final String examUrl;

    @Autowired
    public RemoteExamPageRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                                    @Value("${tds.exam.remote.url}") final String examUrl) {
        this.restTemplate = restTemplate;
        this.examUrl = examUrl;
    }

    @Override
    public ExamPage findPageWithItems(final OpportunityInstance opportunityInstance, final int position) throws ReturnStatusException {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString("{examUrl}/{examId}/page{position}")
            .queryParam("sessionId", opportunityInstance.getSessionKey())
            .queryParam("browserId", opportunityInstance.getExamBrowserKey())
            .buildAndExpand(examUrl, opportunityInstance.getExamId(), position);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
        ResponseEntity<Response<ExamPage>> responseEntity;

        ExamPage examPage = null;

        try {
            responseEntity = restTemplate.exchange(uriComponents.encode().toUri(),
                HttpMethod.GET,
                requestHttpEntity,
                new ParameterizedTypeReference<Response<ExamPage>>() {});

            if (responseEntity.getBody().getData().isPresent()) {
                examPage = responseEntity.getBody().getData().get();
            } else {
                if (responseEntity.getBody().getError().isPresent()) {
                    throw new ReturnStatusException(responseEntity.getBody().getError().get().getMessage());
                }
            }
        } catch(RestClientException rce) {
            throw new ReturnStatusException(rce);
        }

        return examPage;
    }

    @Override
    public List<ExamPage> findAllPagesWithItems(final OpportunityInstance opportunityInstance) throws ReturnStatusException {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString("{examUrl}/{examId}/page")
            .queryParam("sessionId", opportunityInstance.getSessionKey())
            .queryParam("browserId", opportunityInstance.getExamBrowserKey())
            .buildAndExpand(examUrl, opportunityInstance.getExamId());

        try {
            ExamPage[] examPages = restTemplate.getForObject(uriComponents.encode().toUri(), ExamPage[].class);

            return newArrayList(examPages);
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }
}
