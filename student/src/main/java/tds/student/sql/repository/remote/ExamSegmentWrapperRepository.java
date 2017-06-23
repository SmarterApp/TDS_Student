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
import com.google.common.base.Optional;

import java.util.List;
import java.util.UUID;

import tds.exam.wrapper.ExamSegmentWrapper;

/**
 * Handles finding data for {@link tds.exam.wrapper.ExamSegmentWrapper}
 */
public interface ExamSegmentWrapperRepository {
  /**
   * Finds all the {@link tds.exam.wrapper.ExamSegmentWrapper} for an exam
   *
   * @param examId the exam id
   * @return a list of all {@link tds.exam.wrapper.ExamSegmentWrapper} for an exam
   * @throws ReturnStatusException if there is any issue finding the data
   */
  List<ExamSegmentWrapper> findAllExamSegmentWrappersForExam(final UUID examId) throws ReturnStatusException;

  /**
   * Finds the {@link tds.exam.wrapper.ExamSegmentWrapper} for an exam and a specific page position
   *
   * @param examId       exam id
   * @param pagePosition page position to find the page and items
   * @return a single {@link tds.exam.wrapper.ExamSegmentWrapper} containing a single {@link tds.exam.wrapper.ExamPageWrapper}
   * @throws ReturnStatusException if there are any issues fetching the data
   */
  Optional<ExamSegmentWrapper> findExamSegmentWrappersForExamAndPagePosition(final UUID examId, int pagePosition) throws ReturnStatusException;
}
