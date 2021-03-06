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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

import tds.student.services.abstractions.IAdaptiveService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.repository.remote.ExamItemResponseRepository;

@Service("integrationAdaptiveService")
@Scope("prototype")
public class RemoteAdaptiveService implements IAdaptiveService {
  private final IAdaptiveService legacyAdaptiveService;
  private final boolean remoteExamCallsEnabled;
  private final boolean legacyCallsEnabled;
  private final ExamItemResponseRepository examItemResponseRepository;

  @Autowired
  public RemoteAdaptiveService(@Qualifier("legacyAdaptiveService") final IAdaptiveService legacyAdaptiveService,
                               @Value("${tds.exam.remote.enabled}") Boolean remoteExamCallsEnabled,
                               @Value("${tds.exam.legacy.enabled}") Boolean legacyCallsEnabled,
                               final ExamItemResponseRepository examItemResponseRepository) {
    if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
      throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration");
    }

    this.legacyAdaptiveService = legacyAdaptiveService;
    this.remoteExamCallsEnabled = remoteExamCallsEnabled;
    this.legacyCallsEnabled = legacyCallsEnabled;
    this.examItemResponseRepository = examItemResponseRepository;
  }

  @Override
  public PageGroup createNextItemGroup(final TestOpportunity testOpportunity, final int lastPage, final int lastPosition) throws ReturnStatusException {
    PageGroup pageGroup = null;

    if (legacyCallsEnabled) {
      pageGroup = legacyAdaptiveService.createNextItemGroup(testOpportunity, lastPage, lastPosition);
    }

    if (!remoteExamCallsEnabled) {
      return pageGroup;
    }

    final List<OpportunityItem> items = examItemResponseRepository.createNextItemGroup(testOpportunity.getOppInstance().getExamId(), lastPage, lastPosition);

    final PageGroup remotePageGroup = PageGroup.Create(items);

    if (remotePageGroup != null) {
      for (final ItemResponse response : remotePageGroup) {
        response.setPrefetched(true);
      }
    }

    if(!legacyCallsEnabled) {
      return remotePageGroup;
    }

    //TODO - Since this data is used to insert records into the DB we will need to manually move the code over to use
    return pageGroup;
  }
}
