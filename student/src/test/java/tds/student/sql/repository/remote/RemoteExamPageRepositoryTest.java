package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.collect.Lists;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tds.assessment.AssessmentWindow;
import tds.common.Response;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.impl.RemoteExamPageRepository;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteExamPageRepositoryTest {

    @Mock
    private RestTemplate mockRestTemplate;

    private ExamPageRepository remoteExamPageRepository;
    private final OpportunityInstance mockOpportunityInstance = new OpportunityInstance(UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID());

    @Before
    public void setUp() {
        remoteExamPageRepository = new RemoteExamPageRepository(mockRestTemplate, "http://localhost:8080/exam");
    }

    @Test
    public void shouldReturnManyExamPagesWithExamItemsForAnExam() throws ReturnStatusException {
        final UUID examId = UUID.randomUUID();
        final UUID firstExamPageId = UUID.randomUUID();
        final UUID secondExamPageId = UUID.randomUUID();

        final ExamItem firstExamItem = new ExamItem.Builder(UUID.randomUUID())
            .withExamPageId(firstExamPageId)
            .withItemKey("itemKey")
            .withItemFilePath("filePath")
            .withItemType("itemType")
            .build();
        final ExamItem secondExamItem = new ExamItem.Builder(UUID.randomUUID())
            .withExamPageId(secondExamPageId)
            .withItemKey("itemKey2")
            .withItemFilePath("filePath2")
            .withItemType("itemType2")
            .build();

        final ExamPage firstExamPage = new ExamPage.Builder()
            .withId(firstExamPageId)
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withItemGroupKey("itemGroup")
            .withExamItems(newArrayList(firstExamItem))
            .build();
        final ExamPage secondExamPage = new ExamPage.Builder()
            .withId(secondExamPageId)
            .withExamId(examId)
            .withSegmentKey("segmentKey2")
            .withItemGroupKey("itemGroup2")
            .withExamItems(newArrayList(secondExamItem))
            .build();

        ExamPage[] examPageResult = new ExamPage[2];
        examPageResult[0] = firstExamPage;
        examPageResult[1] = secondExamPage;

        when(mockRestTemplate.getForObject(any(URI.class), eq(ExamPage[].class)))
            .thenReturn(examPageResult);

        List<ExamPage> result = remoteExamPageRepository.findAllPagesWithItems(mockOpportunityInstance);
        verify(mockRestTemplate).getForObject(any(URI.class), eq(ExamPage[].class));

        assertThat(result).hasSize(2);
    }

    @Test
    public void shouldGetAnExamPageWithItems() throws ReturnStatusException, URISyntaxException {
        final UUID examId = UUID.randomUUID();
        final UUID examPageId = UUID.randomUUID();

        final ExamItem examItem = new ExamItem.Builder(UUID.randomUUID())
            .withExamPageId(examPageId)
            .withItemKey("itemKey")
            .withItemFilePath("filePath")
            .withItemType("itemType")
            .build();
        final ExamPage examPage = new ExamPage.Builder()
            .withId(examPageId)
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withItemGroupKey("itemGroup")
            .withExamItems(newArrayList(examItem))
            .build();

        final ResponseEntity<ExamPage> responseEntity = new ResponseEntity<>(examPage, HttpStatus.OK);

        URI uri = new URI(String.format("http://localhost:8080/exam/%s/page/%d", examId, 1));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

        when(mockRestTemplate.exchange(uri,
            HttpMethod.GET,
            requestHttpEntity,
          new ParameterizedTypeReference<ExamPage>() {
          })).thenReturn(responseEntity);

        ExamPage result = remoteExamPageRepository.findPageWithItems(mockOpportunityInstance, 1);

        assertThat(result.getId()).isEqualTo(examPage.getId());
        assertThat(result.getExamId()).isEqualTo(examPage.getExamId());
        assertThat(result.getExamItems()).hasSize(1);
    }
}
