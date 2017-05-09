package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.UUID;

import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.wrapper.ExamPageWrapper;
import tds.exam.wrapper.ExamSegmentWrapper;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.repository.remote.ExamItemResponseRepository;
import tds.student.sql.repository.remote.ExamSegmentRepository;
import tds.student.sql.repository.remote.ExamSegmentWrapperRepository;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteResponseServiceTest {
  @Mock
  private IResponseService mockLegacyResponseService;

  @Mock
  private ExamSegmentRepository mockExamSegmentRepository;

  @Mock
  private ExamItemResponseRepository mockExamItemResponseRepository;

  @Mock
  private ExamSegmentWrapperRepository mockExamSegmentWrapperRepository;

  private IResponseService service;

  private OpportunityInstance opportunityInstance;
  private OpportunityItem opportunityItem;
  private PageList pageList;
  private PageGroup pageGroup;

  private ExamSegmentWrapper examSegmentWrapper;
  private UUID examId;

  @Before
  public void setUp() {
    service = new RemoteResponseService(mockLegacyResponseService,
      true,
      false,
      mockExamSegmentRepository,
      mockExamItemResponseRepository,
      mockExamSegmentWrapperRepository);

    examId = UUID.randomUUID();

    opportunityInstance = new OpportunityInstance(UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      examId,
      UUID.randomUUID(),
      "SBAC_PT",
      "browserAgent");

    // Build test data for legacy data
    opportunityItem = new OpportunityItem();
    opportunityItem.setValue("unit test item");
    opportunityItem.setPosition(1);
    opportunityItem.setItemKey(1234L);
    opportunityItem.setBankKey(187);
    opportunityItem.setPage(1);
    opportunityItem.setGroupID("group id");
    opportunityItem.setSegmentID("segment id");
    opportunityItem.setGroupItemsRequired(-1);
    opportunityItem.setStimulusFile("stimulus file path");

    pageList = PageList.Create(newArrayList(opportunityItem));

    pageGroup = PageGroup.Create(newArrayList(opportunityItem));

    UUID pageId = UUID.randomUUID();

    // Build test data representing response from the RemoteExamPageRepository
    ExamItemResponse examItemResponse = new ExamItemResponse.Builder()
      .withExamItemId(UUID.randomUUID())
      .withResponse(opportunityItem.getValue())
      .withCreatedAt(Instant.now())
      .build();

    ExamItem examItem = new ExamItem.Builder(UUID.randomUUID())
      .withItemKey("itemKey")
      .withItemType("itemType")
      .withItemFilePath("filePath")
      .withAssessmentItemBankKey(opportunityItem.getBankKey())
      .withAssessmentItemKey(opportunityItem.getItemKey())
      .withPosition(opportunityItem.getPosition())
      .withResponse(examItemResponse)
      .withCreatedAt(Instant.now())
      .withExamPageId(pageId)
      .withStimulusFilePath(opportunityItem.getStimulusFile())
      .withRequired(true)
      .build();

    ExamPage examPage = new ExamPage.Builder()
      .withId(pageId)
      .withExamId(examId)
      .withPagePosition(opportunityItem.getPage())
      .withCreatedAt(Instant.now())
      .withItemGroupKey(opportunityItem.getGroupID())
      .withSegmentKey("segment key")
      .withGroupItemsRequired(-1)
      .build();

    ExamSegment examSegment = new ExamSegment.Builder()
      .withExamId(examId)
      .withSegmentId("segment id")
      .withSegmentKey("segment key")
      .withSegmentPosition(1)
      .build();

    ExamPageWrapper examPageWrapper = new ExamPageWrapper(examPage, Collections.singletonList(examItem));
    examSegmentWrapper = new ExamSegmentWrapper(examSegment, Collections.singletonList(examPageWrapper));
  }

  @Test
  public void shouldCheckIfExamSegmentsComplete() throws ReturnStatusException {
    when(mockExamSegmentRepository.checkSegmentsSatisfied(examId)).thenReturn(false);

    boolean isComplete = service.isTestComplete(opportunityInstance);
    assertThat(isComplete).isFalse();

    verifyZeroInteractions(mockLegacyResponseService);
    verify(mockExamSegmentRepository).checkSegmentsSatisfied(examId);
  }

  @Test
  public void shouldCheckIfLegacyAndRemoteExamSegmentsComplete() throws ReturnStatusException {
    service = new RemoteResponseService(mockLegacyResponseService,
      true,
      true,
      mockExamSegmentRepository,
      mockExamItemResponseRepository,
      mockExamSegmentWrapperRepository);

    when(mockExamSegmentRepository.checkSegmentsSatisfied(examId)).thenReturn(false);

    boolean isComplete = service.isTestComplete(opportunityInstance);
    assertThat(isComplete).isFalse();

    verify(mockLegacyResponseService).isTestComplete(opportunityInstance);
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
  public void shouldGetPageListWhenLegacyCallsAndRemoteCallsAreEnabled() throws ReturnStatusException {
    service = new RemoteResponseService(mockLegacyResponseService,
      true,
      true,
      mockExamSegmentRepository,
      mockExamItemResponseRepository,
      mockExamSegmentWrapperRepository);

    when(mockLegacyResponseService.getOpportunityItems(any(OpportunityInstance.class), anyBoolean()))
      .thenReturn(pageList);
    when(mockExamSegmentWrapperRepository.findAllExamSegmentWrappersForExam(examId))
      .thenReturn(newArrayList(examSegmentWrapper));

    PageList result = service.getOpportunityItems(opportunityInstance, true);
    verify(mockLegacyResponseService).getOpportunityItems(opportunityInstance, true);
    verify(mockExamSegmentWrapperRepository).findAllExamSegmentWrappersForExam(examId);

    // When the PageList is created, it calls PageGroup#Create, which converts the OpportunityItem into an ItemResponse.
    // Therefore, the OpportunityItem created during setUp() is compared against the ItemResponse contained within the
    // PageGroup (which is itself contained within the PageList).

    // Verify the PageList only has one PageGroup
    assertThat(result).hasSize(1);

    PageGroup pageGroup = result.get(0);
    ExamPage examPage = examSegmentWrapper.getExamPages().get(0).getExamPage();
    assertThat(pageGroup.getNumber()).isEqualTo(examPage.getPagePosition());
    assertThat(pageGroup.getGroupID()).isEqualTo(examPage.getItemGroupKey());
    assertThat(pageGroup.getSegmentPos()).isEqualTo(examSegmentWrapper.getExamSegment().getSegmentPosition());
    assertThat(pageGroup.getSegmentID()).isEqualTo(examSegmentWrapper.getExamSegment().getSegmentId());
    assertThat(pageGroup.getItemsRequired()).isEqualTo(1);
    assertThat(pageGroup.getNumRequired()).isEqualTo(-1);

    // Verify the PageGroup only has one ItemResponse
    assertThat(pageGroup).hasSize(1);

    ItemResponse itemResponse = result.get(0).get(0);
    assertThat(itemResponse.getBankKey()).isEqualTo(opportunityItem.getBankKey());
    assertThat(itemResponse.getItemKey()).isEqualTo(opportunityItem.getItemKey());
    assertThat(itemResponse.getValue()).isEqualTo(opportunityItem.getValue());
    assertThat(itemResponse.getPosition()).isEqualTo(opportunityItem.getPosition());
    assertThat(itemResponse.getPage()).isEqualTo(opportunityItem.getPage());
  }

  @Test
  public void shouldGetPageListWhenLegacyCallsAreEnabledButRemoteCallsAreDisabled() throws ReturnStatusException {
    service = new RemoteResponseService(mockLegacyResponseService,
      false,
      true,
      mockExamSegmentRepository,
      mockExamItemResponseRepository,
      mockExamSegmentWrapperRepository);

    when(mockLegacyResponseService.getOpportunityItems(any(OpportunityInstance.class), anyBoolean()))
      .thenReturn(pageList);

    PageList result = service.getOpportunityItems(opportunityInstance, true);
    verify(mockLegacyResponseService).getOpportunityItems(opportunityInstance, true);
    verifyZeroInteractions(mockExamSegmentWrapperRepository);

    // When the PageList is created, it calls PageGroup#Create, which converts the OpportunityItem into an ItemResponse.
    // Therefore, the OpportunityItem created during setUp() is compared against the ItemResponse contained within the
    // PageGroup (which is itself contained within the PageList).

    // Verify the PageList only has one PageGroup
    assertThat(result).containsExactly(pageGroup);

    // Verify the PageGroup only has one ItemResponse
    ItemResponse itemResponse = result.get(0).get(0);
    assertThat(itemResponse.getBankKey()).isEqualTo(opportunityItem.getBankKey());
    assertThat(itemResponse.getItemKey()).isEqualTo(opportunityItem.getItemKey());
    assertThat(itemResponse.getValue()).isEqualTo(opportunityItem.getValue());
    assertThat(itemResponse.getPosition()).isEqualTo(opportunityItem.getPosition());
    assertThat(itemResponse.getPage()).isEqualTo(opportunityItem.getPage());
  }

  @Test
  public void shouldGetPageListWhenLegacyCallsAreDisabledButRemoteCallsAReEnabled() throws ReturnStatusException {
    service = new RemoteResponseService(mockLegacyResponseService,
      true,
      false,
      mockExamSegmentRepository,
      mockExamItemResponseRepository,
      mockExamSegmentWrapperRepository);

    when(mockExamSegmentWrapperRepository.findAllExamSegmentWrappersForExam(examId))
      .thenReturn(Collections.singletonList(examSegmentWrapper));

    PageList result = service.getOpportunityItems(opportunityInstance, true);
    verifyZeroInteractions(mockLegacyResponseService);
    verify(mockExamSegmentWrapperRepository).findAllExamSegmentWrappersForExam(examId);

    // When the PageList is created, it calls PageGroup#Create, which converts the OpportunityItem into an ItemResponse.
    // Therefore, the OpportunityItem created during setUp() is compared against the ItemResponse contained within the
    // PageGroup (which is itself contained within the PageList).

    // Verify the PageList only has one PageGroup
    assertThat(result).hasSize(1);

    PageGroup pageGroup = result.get(0);
    ExamPage examPage = examSegmentWrapper.getExamPages().get(0).getExamPage();
    assertThat(pageGroup.getNumber()).isEqualTo(examPage.getPagePosition());
    assertThat(pageGroup.getGroupID()).isEqualTo(examPage.getItemGroupKey());
    assertThat(pageGroup.getSegmentPos()).isEqualTo(examSegmentWrapper.getExamSegment().getSegmentPosition());
    assertThat(pageGroup.getSegmentID()).isEqualTo(examSegmentWrapper.getExamSegment().getSegmentId());
    assertThat(pageGroup.getItemsRequired()).isEqualTo(1);

    // Verify the PageGroup only has one ItemResponse
    assertThat(pageGroup).hasSize(1);

    ItemResponse itemResponse = result.get(0).get(0);
    assertThat(itemResponse.getBankKey()).isEqualTo(opportunityItem.getBankKey());
    assertThat(itemResponse.getItemKey()).isEqualTo(opportunityItem.getItemKey());
    assertThat(itemResponse.getValue()).isEqualTo(opportunityItem.getValue());
    assertThat(itemResponse.getPosition()).isEqualTo(opportunityItem.getPosition());
    assertThat(itemResponse.getPage()).isEqualTo(opportunityItem.getPage());
  }

  @Test
  public void shouldGetPageGroupWhenLegacyCallsAndRemoteCallsAreEnabled() throws ReturnStatusException {
    service = new RemoteResponseService(mockLegacyResponseService,
      true,
      true,
      mockExamSegmentRepository,
      mockExamItemResponseRepository,
      mockExamSegmentWrapperRepository);

    when(mockLegacyResponseService.getItemGroup(any(OpportunityInstance.class),
      anyInt(),
      anyString(),
      anyString(),
      anyBoolean()))
      .thenReturn(pageGroup);
    when(mockExamSegmentWrapperRepository.findExamSegmentWrappersForExamAndPagePosition(examId, 1))
      .thenReturn(Optional.of(examSegmentWrapper));

    PageGroup result = service.getItemGroup(opportunityInstance,
      1,
      "group id",
      Instant.now().toString(),
      true);

    verify(mockLegacyResponseService).getItemGroup(any(OpportunityInstance.class),
      anyInt(),
      anyString(),
      anyString(),
      anyBoolean());
    verify(mockExamSegmentWrapperRepository).findExamSegmentWrappersForExamAndPagePosition(examId, 1);

    ExamPage examPage = examSegmentWrapper.getExamPages().get(0).getExamPage();
    assertThat(result.getNumber()).isEqualTo(examPage.getPagePosition());
    assertThat(result.getGroupID()).isEqualTo(examPage.getItemGroupKey());
    assertThat(result.getSegmentPos()).isEqualTo(examSegmentWrapper.getExamSegment().getSegmentPosition());
    assertThat(result.getSegmentID()).isEqualTo(examSegmentWrapper.getExamSegment().getSegmentId());
    assertThat(result.getItemsRequired()).isEqualTo(1);
    assertThat(result).hasSize(1);

    // When the PageList is created, it calls PageGroup#Create, which converts the OpportunityItem into an ItemResponse.
    // Therefore, the OpportunityItem created during setUp() is compared against the ItemResponse contained within the
    // PageGroup (which is itself contained within the PageList).

    ItemResponse itemResponse = result.get(0);
    assertThat(itemResponse.getBankKey()).isEqualTo(opportunityItem.getBankKey());
    assertThat(itemResponse.getItemKey()).isEqualTo(opportunityItem.getItemKey());
    assertThat(itemResponse.getValue()).isEqualTo(opportunityItem.getValue());
    assertThat(itemResponse.getPosition()).isEqualTo(opportunityItem.getPosition());
    assertThat(itemResponse.getPage()).isEqualTo(opportunityItem.getPage());
  }

  @Test
  public void shouldGetPageGroupWhenLegacyCallsAreEnabledButRemoteCallsAreDisabled() throws ReturnStatusException {
    service = new RemoteResponseService(mockLegacyResponseService,
      false,
      true,
      mockExamSegmentRepository,
      mockExamItemResponseRepository,
      mockExamSegmentWrapperRepository);

    when(mockLegacyResponseService.getItemGroup(any(OpportunityInstance.class),
      anyInt(),
      anyString(),
      anyString(),
      anyBoolean()))
      .thenReturn(pageGroup);

    PageGroup result = service.getItemGroup(opportunityInstance,
      1,
      "group id",
      Instant.now().toString(),
      true);

    verify(mockLegacyResponseService).getItemGroup(any(OpportunityInstance.class),
      anyInt(),
      anyString(),
      anyString(),
      anyBoolean());
    verifyZeroInteractions(mockExamSegmentWrapperRepository);

    // When the PageList is created, it calls PageGroup#Create, which converts the OpportunityItem into an ItemResponse.
    // Therefore, the OpportunityItem created during setUp() is compared against the ItemResponse contained within the
    // PageGroup (which is itself contained within the PageList).

    assertThat(result).isEqualTo(pageGroup);
    ItemResponse itemResponse = result.get(0);
    assertThat(itemResponse.getBankKey()).isEqualTo(opportunityItem.getBankKey());
    assertThat(itemResponse.getItemKey()).isEqualTo(opportunityItem.getItemKey());
    assertThat(itemResponse.getValue()).isEqualTo(opportunityItem.getValue());
    assertThat(itemResponse.getPosition()).isEqualTo(opportunityItem.getPosition());
    assertThat(itemResponse.getPage()).isEqualTo(opportunityItem.getPage());
  }

  @Test
  public void shouldGetPageGroupWhenLegacyCallsAreDisabledButRemoteCallsAreEnabled() throws ReturnStatusException {
    service = new RemoteResponseService(mockLegacyResponseService,
      true,
      false,
      mockExamSegmentRepository,
      mockExamItemResponseRepository,
      mockExamSegmentWrapperRepository);

    when(mockExamSegmentWrapperRepository.findExamSegmentWrappersForExamAndPagePosition(examId, 1))
      .thenReturn(Optional.of(examSegmentWrapper));

    PageGroup result = service.getItemGroup(opportunityInstance,
      1,
      "group id",
      Instant.now().toString(),
      true);

    verifyZeroInteractions(mockLegacyResponseService);
    verify(mockExamSegmentWrapperRepository).findExamSegmentWrappersForExamAndPagePosition(examId, 1);

    ExamPage examPage = examSegmentWrapper.getExamPages().get(0).getExamPage();
    assertThat(result.getNumber()).isEqualTo(examPage.getPagePosition());
    assertThat(result.getGroupID()).isEqualTo(examPage.getItemGroupKey());
    assertThat(result.getSegmentPos()).isEqualTo(examSegmentWrapper.getExamSegment().getSegmentPosition());
    assertThat(result.getSegmentID()).isEqualTo(examSegmentWrapper.getExamSegment().getSegmentId());
    assertThat(result).hasSize(1);

    // When the PageList is created, it calls PageGroup#Create, which converts the OpportunityItem into an ItemResponse.
    // Therefore, the OpportunityItem created during setUp() is compared against the ItemResponse contained within the
    // PageGroup (which is itself contained within the PageList).

    ItemResponse itemResponse = result.get(0);
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
      mockExamSegmentWrapperRepository);
  }
}
