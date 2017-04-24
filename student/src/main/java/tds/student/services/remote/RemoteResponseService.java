package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.sql.data.AdaptiveGroup;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.repository.remote.ExamItemResponseRepository;
import tds.student.sql.repository.remote.ExamPageRepository;
import tds.student.sql.repository.remote.ExamSegmentRepository;

@Service("integrationResponseService")
@Scope("prototype")
public class RemoteResponseService implements IResponseService {
  private final boolean isRemoteExamCallsEnabled;
  private final boolean isLegacyCallsEnabled;
  private final IResponseService legacyResponseService;
  private final ExamSegmentRepository examSegmentRepository;
  private final ExamItemResponseRepository examItemResponseRepository;
  private final ExamPageRepository examPageRepository;

  @Autowired
  public RemoteResponseService(final IResponseService legacyResponseService,
                               final @Value("${tds.exam.remote.enabled}") Boolean remoteExamCallsEnabled,
                               final @Value("${tds.exam.legacy.enabled}") Boolean legacyCallsEnabled,
                               final ExamSegmentRepository examSegmentRepository,
                               final ExamItemResponseRepository examItemResponseRepository,
                               final ExamPageRepository examPageRepository) {
    if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
      throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration for 'tds.exam.remote.enabled' and 'tds.exam.legacy.enabled' settings");
    }

    this.legacyResponseService = legacyResponseService;
    this.examSegmentRepository = examSegmentRepository;
    this.isRemoteExamCallsEnabled = remoteExamCallsEnabled;
    this.isLegacyCallsEnabled = legacyCallsEnabled;
    this.examItemResponseRepository = examItemResponseRepository;
    this.examPageRepository = examPageRepository;
  }

  @Override
  public PageGroup insertItems(final OpportunityInstance oppInstance, final AdaptiveGroup adaptiveGroup, final boolean isMsb) throws ReturnStatusException {
    return legacyResponseService.insertItems(oppInstance, adaptiveGroup, isMsb);
  }

  @Override
  public PageList getOpportunityItems(final OpportunityInstance oppInstance,
                                      final boolean validate) throws ReturnStatusException {
    PageList pageList = null;

    if (isLegacyCallsEnabled) {
      pageList = legacyResponseService.getOpportunityItems(oppInstance, validate);
    }

    // TODO: Add this code back in when items/pages can be fetched from the exam service
//    if (!isRemoteExamCallsEnabled) {
      return pageList;
//    }

//    List<ExamPage> examPagesWithItems = examPageRepository.findAllPagesWithItems(oppInstance);
//
//    return PageList.Create(convertExamPagesToOpportunityItems(examPagesWithItems.toArray(new ExamPage[examPagesWithItems.size()])));
  }

  @Override
  public PageGroup getItemGroup(final OpportunityInstance oppInstance,
                                final int page,
                                final String groupID,
                                final String dateCreated,
                                final boolean validate) throws ReturnStatusException {
    PageGroup pageGroup = null;

    if (isLegacyCallsEnabled) {
      pageGroup = legacyResponseService.getItemGroup(oppInstance, page, groupID, dateCreated, validate);
    }

    // TODO: Add this code back in when items/pages can be fetched from the exam service
//    if (!isRemoteExamCallsEnabled) {
      return pageGroup;
//    }

//    ExamPage examPageWithItems = examPageRepository.findPageWithItems(oppInstance, page);
//
//    return PageGroup.Create(convertExamPagesToOpportunityItems(examPageWithItems));
  }

  @Override
  public boolean isTestComplete(final UUID examId) throws ReturnStatusException {
    boolean isComplete = false;

    if (isLegacyCallsEnabled) {
      isComplete = legacyResponseService.isTestComplete(examId);
    }

//    if (!isRemoteExamCallsEnabled) {
      return isComplete;
//    }

//    return examSegmentRepository.checkSegmentsSatisfied(examId);
  }

  @Override
  public void removeResponse(final OpportunityInstance oppInstance, final int position, final String itemID, final String dateCreated) throws ReturnStatusException {
    legacyResponseService.removeResponse(oppInstance, position, itemID, dateCreated);
  }

  @Override
  public void markItemForReview(final OpportunityInstance opportunityInstance, final int position, final boolean mark) throws ReturnStatusException {
    if (isLegacyCallsEnabled) {
      legacyResponseService.markItemForReview(opportunityInstance, position, mark);
    }

    if (!isRemoteExamCallsEnabled) {
      return;
    }

    examItemResponseRepository.markItemForReview(opportunityInstance, position, mark);
  }

  /**
   * Flatten the {@link tds.exam.ExamPage}s and their constituent {@link tds.exam.ExamItem}s and map them to a
   * {@link tds.student.services.data.PageList}.
   *
   * @param examPages The collection of {@link tds.exam.ExamPage}s from the {@link tds.exam.Exam}
   * @return A {@link tds.student.services.data.PageList} that represents the collection of {@link tds.exam.ExamPage}s
   */
  private static List<OpportunityItem> convertExamPagesToOpportunityItems(ExamPage... examPages) {
    List<OpportunityItem> opportunityItems = new ArrayList<>();

    // Match the datetime format returned by t_getopportunityitems
    DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    for (ExamPage page : examPages) {
      for (ExamItem item : page.getExamItems()) {
        OpportunityItem opportunityItem = new OpportunityItem();
        opportunityItem.setBankKey(item.getAssessmentItemBankKey());
        opportunityItem.setItemKey(item.getAssessmentItemKey());
        opportunityItem.setPosition(item.getPosition());
        opportunityItem.setPage(page.getPagePosition());
        opportunityItem.setGroupID(page.getItemGroupKey());
        opportunityItem.setSegment(page.getSegmentPosition());
        opportunityItem.setSegmentID(page.getSegmentId());
        opportunityItem.setGroupItemsRequired(page.isGroupItemsRequired() ? -1 : 0);
        opportunityItem.setIsRequired(item.isRequired());
        opportunityItem.setItemFile(item.getItemFilePath());
        opportunityItem.setStimulusFile(item.getStimulusFilePath().isPresent() ? item.getStimulusFilePath().get() : null);
        opportunityItem.setDateCreated(item.getCreatedAt().toString(dateFormatter));

        // This value is always set to 'format' in the t_getopportunityitems stored procedure, then converted to
        // upper-case in ResponseRepository#readOpportunityItems() (@ line 295).
        opportunityItem.setFormat("FORMAT");

        // OpportunityItem#isVisible (mapped to an OpportunityItem in ResponseRepository#readOpportunityItems @ line
        // 297) never seems to be used (which is why it was not ported to the exam database), so has been omitted from
        // this mapping.

        if (item.getResponse().isPresent()) {
          ExamItemResponse itemResponse = item.getResponse().get();
          opportunityItem.setSequence(itemResponse.getSequence());
          opportunityItem.setMarkForReview(itemResponse.isMarkedForReview());
          opportunityItem.setValue(itemResponse.getResponse());
          opportunityItem.setIsValid(itemResponse.isValid());
          opportunityItem.setIsSelected(itemResponse.isSelected());
        } else {
          opportunityItem.setValue(null);
        }

        opportunityItems.add(opportunityItem);
      }
    }

    return opportunityItems;
  }
}
