package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.collect.Lists;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.repository.remote.ExamItemResponseRepository;
import tds.student.sql.repository.remote.ExamPageRepository;
import tds.student.sql.repository.remote.ExamSegmentRepository;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verifyZeroInteractions;
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

  @Mock
  private ExamPageRepository mockExamPageRepository;

  private IResponseService service;

  private final OpportunityInstance opportunityInstance = new OpportunityInstance(UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID());
  private OpportunityItem opportunityItem;
  private PageList pageList;
  private ExamPage examPage;

  @Before
  public void setUp() {
    service = new RemoteResponseService(mockLegacyResponseService,
        true,
        false,
        mockExamSegmentRepository,
        mockExamItemResponseRepository,
        mockExamPageRepository);

    // Build test data for legacy data
    opportunityItem = new OpportunityItem();
    opportunityItem.setValue("unit test item");
    opportunityItem.setPosition(1);
    opportunityItem.setItemKey(1234L);
    opportunityItem.setBankKey(187);
    opportunityItem.setPage(1);

    pageList = PageList.Create(newArrayList(opportunityItem));

    // Build test data representing response from the RemoteExamPageRepository
    ExamItemResponse examItemResponse = new ExamItemResponse.Builder()
        .withResponse(opportunityItem.getValue())
        .withCreatedAt(Instant.now())
        .build();

    ExamItem examItem = new ExamItem.Builder(UUID.randomUUID())
        .withAssessmentItemBankKey(opportunityItem.getBankKey())
        .withAssessmentItemKey(opportunityItem.getItemKey())
        .withPosition(opportunityItem.getPosition())
        .withResponse(examItemResponse)
        .withCreatedAt(Instant.now())
        .build();

    examPage = new ExamPage.Builder()
        .withPagePosition(opportunityItem.getPage())
        .withExamItems(newArrayList(examItem))
        .withCreatedAt(Instant.now())
        .build();
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
    final int position = 3;
    final boolean mark = true;

    service.markItemForReview(opportunityInstance, position, mark);
    verify(mockExamItemResponseRepository).markItemForReview(opportunityInstance, position, mark);
  }

  @Test
  public void shouldGetOpportunityItemsForAnExamWhenLegacyCallsAndRemoteCallsAreEnabled() throws ReturnStatusException {
    service = new RemoteResponseService(mockLegacyResponseService,
        true,
        true,
        mockExamSegmentRepository,
        mockExamItemResponseRepository,
        mockExamPageRepository);

    when(mockLegacyResponseService.getOpportunityItems(any(OpportunityInstance.class), any(Boolean.class)))
        .thenReturn(pageList);
    when(mockExamPageRepository.findAllPagesWithItems(any(OpportunityInstance.class)))
        .thenReturn(newArrayList(examPage));

    PageList result = service.getOpportunityItems(opportunityInstance, true);
    verify(mockLegacyResponseService).getOpportunityItems(opportunityInstance, true);
    verify(mockExamPageRepository).findAllPagesWithItems(opportunityInstance);

    // When the PageList is created, it calls PageGroup#Create, which converts the OpportunityItem into an ItemResponse.
    // Therefore, the OpportunityItem created during setUp() is compared against the ItemResponse contained within the
    // PageGroup (which is itself contained within the PageList).

    // Verify the PageList only has one PageGroup
    assertThat(result).hasSize(1);

    // Verify the PageGroup only has one ItemResponse
    PageGroup pageGroup = result.get(0);
    assertThat(pageGroup).hasSize(1);

    ItemResponse itemResponse = result.get(0).get(0);
    assertThat(itemResponse.getBankKey()).isEqualTo(opportunityItem.getBankKey());
    assertThat(itemResponse.getItemKey()).isEqualTo(opportunityItem.getItemKey());
    assertThat(itemResponse.getValue()).isEqualTo(opportunityItem.getValue());
    assertThat(itemResponse.getPosition()).isEqualTo(opportunityItem.getPosition());
    assertThat(itemResponse.getPage()).isEqualTo(opportunityItem.getPage());
  }

  @Test
  public void shouldGetOpportunityItemsForAnExamWhenLegacyCallsAreEnabledButRemoteCallsAreDisabled() throws ReturnStatusException {
    service = new RemoteResponseService(mockLegacyResponseService,
        false,
        true,
        mockExamSegmentRepository,
        mockExamItemResponseRepository,
        mockExamPageRepository);

    when(mockLegacyResponseService.getOpportunityItems(any(OpportunityInstance.class), any(Boolean.class)))
        .thenReturn(pageList);

    PageList result = service.getOpportunityItems(opportunityInstance, true);
    verify(mockLegacyResponseService).getOpportunityItems(opportunityInstance, true);
    verifyZeroInteractions(mockExamPageRepository);

    // When the PageList is created, it calls PageGroup#Create, which converts the OpportunityItem into an ItemResponse.
    // Therefore, the OpportunityItem created during setUp() is compared against the ItemResponse contained within the
    // PageGroup (which is itself contained within the PageList).

    // Verify the PageList only has one PageGroup
    assertThat(result).hasSize(1);

    // Verify the PageGroup only has one ItemResponse
    PageGroup pageGroup = result.get(0);
    assertThat(pageGroup).hasSize(1);

    ItemResponse itemResponse = result.get(0).get(0);
    assertThat(itemResponse.getBankKey()).isEqualTo(opportunityItem.getBankKey());
    assertThat(itemResponse.getItemKey()).isEqualTo(opportunityItem.getItemKey());
    assertThat(itemResponse.getValue()).isEqualTo(opportunityItem.getValue());
    assertThat(itemResponse.getPosition()).isEqualTo(opportunityItem.getPosition());
    assertThat(itemResponse.getPage()).isEqualTo(opportunityItem.getPage());
  }

  @Test
  public void shouldGetOpportunityItemsForAnExamWhenLegacyCallsAreDisabledButRemoteCallsAReEnabled() throws ReturnStatusException {
    service = new RemoteResponseService(mockLegacyResponseService,
        true,
        false,
        mockExamSegmentRepository,
        mockExamItemResponseRepository,
        mockExamPageRepository);

    when(mockExamPageRepository.findAllPagesWithItems(any(OpportunityInstance.class)))
        .thenReturn(newArrayList(examPage));

    PageList result = service.getOpportunityItems(opportunityInstance, true);
    verifyZeroInteractions(mockLegacyResponseService);
    verify(mockExamPageRepository).findAllPagesWithItems(opportunityInstance);

    // When the PageList is created, it calls PageGroup#Create, which converts the OpportunityItem into an ItemResponse.
    // Therefore, the OpportunityItem created during setUp() is compared against the ItemResponse contained within the
    // PageGroup (which is itself contained within the PageList).

    // Verify the PageList only has one PageGroup
    assertThat(result).hasSize(1);

    // Verify the PageGroup only has one ItemResponse
    PageGroup pageGroup = result.get(0);
    assertThat(pageGroup).hasSize(1);

    ItemResponse itemResponse = result.get(0).get(0);
    assertThat(itemResponse.getBankKey()).isEqualTo(opportunityItem.getBankKey());
    assertThat(itemResponse.getItemKey()).isEqualTo(opportunityItem.getItemKey());
    assertThat(itemResponse.getValue()).isEqualTo(opportunityItem.getValue());
    assertThat(itemResponse.getPosition()).isEqualTo(opportunityItem.getPosition());
    assertThat(itemResponse.getPage()).isEqualTo(opportunityItem.getPage());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowIllegalStateExceptionWhenLegacyCallsAndRemoteCallsAreDisabled() {
    new RemoteResponseService(mockLegacyResponseService,
        false,
        false,
        mockExamSegmentRepository,
        mockExamItemResponseRepository,
        mockExamPageRepository);
  }
}
