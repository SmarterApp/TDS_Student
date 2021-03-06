/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.wrapper.ExamPageWrapper;
import tds.exam.wrapper.ExamSegmentWrapper;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.sql.data.AdaptiveGroup;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.repository.remote.ExamItemResponseRepository;
import tds.student.sql.repository.remote.ExamSegmentRepository;
import tds.student.sql.repository.remote.ExamSegmentWrapperRepository;

@Service("integrationResponseService")
@Scope("prototype")
public class RemoteResponseService implements IResponseService {
  private static final Logger LOG = LoggerFactory.getLogger(RemoteResponseService.class);

  private final boolean isRemoteExamCallsEnabled;
  private final boolean isLegacyCallsEnabled;
  private final IResponseService legacyResponseService;
  private final ExamSegmentRepository examSegmentRepository;
  private final ExamItemResponseRepository examItemResponseRepository;
  private final ExamSegmentWrapperRepository examSegmentWrapperRepository;

  @Autowired
  public RemoteResponseService(final @Qualifier("legacyResponseService") IResponseService legacyResponseService,
                               final @Value("${tds.exam.remote.enabled}") Boolean remoteExamCallsEnabled,
                               final @Value("${tds.exam.legacy.enabled}") Boolean legacyCallsEnabled,
                               final ExamSegmentRepository examSegmentRepository,
                               final ExamItemResponseRepository examItemResponseRepository,
                               final ExamSegmentWrapperRepository examSegmentWrapperRepository) {
    if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
      throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration for 'tds.exam.remote.enabled' and 'tds.exam.legacy.enabled' settings");
    }

    this.legacyResponseService = legacyResponseService;
    this.examSegmentRepository = examSegmentRepository;
    this.isRemoteExamCallsEnabled = remoteExamCallsEnabled;
    this.isLegacyCallsEnabled = legacyCallsEnabled;
    this.examItemResponseRepository = examItemResponseRepository;
    this.examSegmentWrapperRepository = examSegmentWrapperRepository;
  }

  @Override
  public PageGroup insertItems(final OpportunityInstance oppInstance, final AdaptiveGroup adaptiveGroup, final boolean isMsb) throws ReturnStatusException {
    return legacyResponseService.insertItems(oppInstance, adaptiveGroup, isMsb);
  }

  @Override
  public PageList getOpportunityItems(final OpportunityInstance oppInstance,
                                      final boolean validate) throws ReturnStatusException {
    PageList legacyPageList = null;

    if (isLegacyCallsEnabled) {
      legacyPageList = legacyResponseService.getOpportunityItems(oppInstance, validate);
    }

    if (!isRemoteExamCallsEnabled) {
      return legacyPageList;
    }

    List<ExamSegmentWrapper> examSegmentWrappers = examSegmentWrapperRepository.findAllExamSegmentWrappersForExam(oppInstance.getExamId());

    return PageList.Create(convertExamPagesToOpportunityItems(examSegmentWrappers));
  }

  @Override
  public PageGroup getItemGroup(final OpportunityInstance oppInstance,
                                final int page,
                                final String groupID,
                                final String dateCreated,
                                final boolean validate) throws ReturnStatusException {
    PageGroup legacyPageGroup = null;

    if (isLegacyCallsEnabled) {
      legacyPageGroup = legacyResponseService.getItemGroup(oppInstance, page, groupID, dateCreated, validate);
    }

    if (!isRemoteExamCallsEnabled) {
      return legacyPageGroup;
    }

    Optional<ExamSegmentWrapper> maybeExamSegmentWrapper = examSegmentWrapperRepository.findExamSegmentWrappersForExamAndPagePosition(oppInstance.getExamId(), page);

    if (!maybeExamSegmentWrapper.isPresent()) {
      throw new ReturnStatusException(String.format("Could not find page for exam id %s and page %d", oppInstance.getExamId(), page));
    }

    return PageGroup.Create(convertExamPagesToOpportunityItems(Collections.singletonList(maybeExamSegmentWrapper.get())));
  }

  @Override
  public boolean isTestComplete(OpportunityInstance opportunityInstance) throws ReturnStatusException {
    boolean isComplete = false;

    if (isLegacyCallsEnabled) {
      isComplete = legacyResponseService.isTestComplete(opportunityInstance);
    }

    if (!isRemoteExamCallsEnabled) {
      return isComplete;
    }

    return examSegmentRepository.checkSegmentsSatisfied(opportunityInstance.getExamId());
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
   * @param examSegmentWrappers The collection of {@link tds.exam.wrapper.ExamSegmentWrapper}s from the {@link tds.exam.Exam}
   * @return A {@link tds.student.services.data.PageList} that represents the collection of {@link tds.exam.ExamPage}s
   */
  private static List<OpportunityItem> convertExamPagesToOpportunityItems(List<ExamSegmentWrapper> examSegmentWrappers) {
    List<OpportunityItem> opportunityItems = new ArrayList<>();

    // Match the datetime format returned by t_getopportunityitems
    DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    for (ExamSegmentWrapper examSegmentWrapper : examSegmentWrappers) {
      ExamSegment examSegment = examSegmentWrapper.getExamSegment();
      for (ExamPageWrapper pageWrapper : examSegmentWrapper.getExamPages()) {
        ExamPage page = pageWrapper.getExamPage();
        for (ExamItem item : pageWrapper.getExamItems()) {
          OpportunityItem opportunityItem = new OpportunityItem();
          opportunityItem.setBankKey(item.getAssessmentItemBankKey());
          opportunityItem.setItemKey(item.getAssessmentItemKey());
          opportunityItem.setPosition(item.getPosition());
          opportunityItem.setPage(page.getPagePosition());
          opportunityItem.setGroupID(page.getItemGroupKey());
          opportunityItem.setSegment(examSegment.getSegmentPosition());
          opportunityItem.setSegmentID(examSegment.getSegmentId());
          opportunityItem.setGroupItemsRequired(page.getGroupItemsRequired());
          opportunityItem.setIsRequired(item.isRequired());
          opportunityItem.setItemFile(item.getItemFilePath());
          opportunityItem.setStimulusFile(getStimulusFilePath(item));
          opportunityItem.setDateCreated(item.getCreatedAt().toString(dateFormatter));

          // This value is always set to 'format' in the t_getopportunityitems stored procedure, then converted to
          // upper-case in ResponseRepository#readOpportunityItems() (@ line 295).
          opportunityItem.setFormat(item.getItemType());

          opportunityItem.setIsVisible(page.isVisible());

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
    }

    return opportunityItems;
  }

  /**
   * The legacy expects stimulus filepath to be null if it isn't valid.
   *
   * @param item {@link tds.exam.ExamItem}
   * @return the filepath if valid otherwise null
   */
  private static String getStimulusFilePath(ExamItem item) {
    if (!item.getStimulusFilePath().isPresent()) return null;

    String stimulusFilePath = item.getStimulusFilePath().get();

    return StringUtils.isEmpty(stimulusFilePath) ? null : stimulusFilePath;
  }
}
