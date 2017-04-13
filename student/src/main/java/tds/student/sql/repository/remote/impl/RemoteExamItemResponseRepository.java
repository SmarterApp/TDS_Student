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

import tds.common.web.resources.NoContentResponseResource;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.ExamItemResponseRepository;

@Repository
public class RemoteExamItemResponseRepository implements ExamItemResponseRepository {
    private final RestTemplate restTemplate;
    private final String examUrl;

    @Autowired
    public RemoteExamItemResponseRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                                            @Value("${tds.exam.remote.url}") final String examUrl) {
        this.restTemplate = restTemplate;
        this.examUrl = examUrl;
    }

    @Override
    public void markItemForReview(final OpportunityInstance opportunityInstance, final int itemPosition, final boolean mark) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(mark, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/item/%d/review", examUrl,
            opportunityInstance.getExamId(), itemPosition))
            .queryParam("sessionId", opportunityInstance.getSessionKey())
            .queryParam("browserId", opportunityInstance.getExamBrowserKey());

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
}
