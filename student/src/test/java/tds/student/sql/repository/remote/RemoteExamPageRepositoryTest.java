package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.impl.RemoteExamPageRepository;

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
            .build();
        final ExamItem secondExamItem = new ExamItem.Builder(UUID.randomUUID())
            .withExamPageId(secondExamPageId)
            .build();

        List<ExamItem> firstExamItemList = new ArrayList<>();
        firstExamItemList.add(firstExamItem);
        List<ExamItem> secondExamItemList = new ArrayList<>();
        secondExamItemList.add(secondExamItem);

        final ExamPage firstExamPage = new ExamPage.Builder()
            .withId(firstExamPageId)
            .withExamId(examId)
            .withExamItems(firstExamItemList)
            .build();
        final ExamPage secondExamPage = new ExamPage.Builder()
            .withId(secondExamPageId)
            .withExamId(examId)
            .withExamItems(secondExamItemList)
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
}
