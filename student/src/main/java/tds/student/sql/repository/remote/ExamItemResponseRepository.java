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

package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;
import java.util.UUID;

import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;

/**
 * Repository for interacting with item and response data in Exam Service
 */
public interface ExamItemResponseRepository {

  /**
   * Verifies access and marks an item for review
   *
   * @param opportunityInstance the {@link tds.student.sql.data.OpportunityInstance} containing exam validation data
   * @param position            the item position of the item to mark
   * @param mark                a flag indicating whether the item should be marked or unmarked
   */
  void markItemForReview(OpportunityInstance opportunityInstance, int position, boolean mark) throws ReturnStatusException;

  /**
   * Gets the next item group
   *
   * @param examId           the exam id
   * @param lastPagePosition the last page position fetched
   * @param lastItemPosition the last item position fetched
   * @return List of {@link tds.student.sql.data.OpportunityItem} which compose the group
   * @throws ReturnStatusException if there is any issue
   */
  List<OpportunityItem> createNextItemGroup(final UUID examId, final int lastPagePosition, final int lastItemPosition) throws ReturnStatusException;
}
