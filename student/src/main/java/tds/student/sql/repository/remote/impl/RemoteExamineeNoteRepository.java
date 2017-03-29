package tds.student.sql.repository.remote.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import tds.exam.ExamineeNote;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.ExamineeNoteRepository;

@Repository
public class RemoteExamineeNoteRepository implements ExamineeNoteRepository {
    private final RestTemplate restTemplate;
    private final String examUrl;

    @Autowired
    public RemoteExamineeNoteRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                                        @Value("${tds.exam.remote.url}") final String examUrl) {
        this.restTemplate = restTemplate;
        this.examUrl = examUrl;
    }

    @Override
    public Optional<ExamineeNote> findNoteInExamContext(final OpportunityInstance opportunityInstance) throws ReturnStatusException {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString("{examUrl}/{examId}/note")
            .queryParam("sessionId", opportunityInstance.getSessionKey())
            .queryParam("browserId", opportunityInstance.getBrowserKey())
            .buildAndExpand(examUrl, opportunityInstance.getExamId());
        ResponseEntity<ExamineeNote> responseEntity;

        try {
            responseEntity = restTemplate.getForEntity(uriComponents.encode().toUri(), ExamineeNote.class);
            return Optional.of(responseEntity.getBody());
        } catch (HttpClientErrorException hce) {
            // In the event there is no note/comment associated with the exam, a 404 is returned from the exam service
            // endpoint.  Return an empty Optional to indicate the exam/opportunity does not have a note/comment
            // associated to it.
            if (hce.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return Optional.absent();
            }

            throw new ReturnStatusException(hce);
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }

    @Override
    public void insert(final OpportunityInstance opportunityInstance,
                       final ExamineeNote examineeNote) throws ReturnStatusException {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString("{examUrl}/{examId}/note")
            .queryParam("sessionId", opportunityInstance.getSessionKey())
            .queryParam("browserId", opportunityInstance.getBrowserKey())
            .buildAndExpand(examUrl, examineeNote.getExamId());

        try {
            restTemplate.postForEntity(uriComponents.encode().toUri(), examineeNote, ResponseEntity.class);
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }
}
