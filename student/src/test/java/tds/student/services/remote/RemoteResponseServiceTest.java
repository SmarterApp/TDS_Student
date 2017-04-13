package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import tds.student.services.abstractions.IResponseService;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.ExamItemResponseRepository;
import tds.student.sql.repository.remote.ExamSegmentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RemoteResponseServiceTest {
  @Mock
  private IResponseService mockLegacyResponseService;

  @Mock
  private ExamSegmentRepository mockExamSegmentRepository;

  @Mock
  private ExamItemResponseRepository mockExamItemResponseRepository;

  private IResponseService service;

  @Before
  public void setUp() {
    service = new RemoteResponseService(mockLegacyResponseService, true, false,
        mockExamSegmentRepository, mockExamItemResponseRepository);
  }

  @Ignore
  @Test
  public void shouldCheckIfExamSegmentsComplete() throws ReturnStatusException {
    final UUID examId = UUID.randomUUID();
    when(mockExamSegmentRepository.checkSegmentsSatisfied(examId)).thenReturn(false);
    boolean isComplete = service.isTestComplete(examId);
    assertThat(isComplete).isFalse();
    verify(mockExamSegmentRepository).checkSegmentsSatisfied(examId);
  }

  @Test
  public void shouldMarkForReview() throws ReturnStatusException {
    final OpportunityInstance opportunityInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    final int position = 3;
    final boolean mark = true;

    service.markItemForReview(opportunityInstance, position, mark);
    verify(mockExamItemResponseRepository).markItemForReview(opportunityInstance, position, mark);
  }
}
