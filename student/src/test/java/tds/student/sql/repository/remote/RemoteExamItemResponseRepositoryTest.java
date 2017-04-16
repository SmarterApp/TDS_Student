package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.impl.RemoteExamItemResponseRepository;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;

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

        remoteExamItemResponseRepository.markItemForReview(opportunityInstance, position, mark);
        verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class));
    }
}
