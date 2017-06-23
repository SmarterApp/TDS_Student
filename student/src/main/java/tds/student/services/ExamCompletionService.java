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

package tds.student.services;

import AIR.Common.data.ResponseData;
import TDS.Shared.Exceptions.ReturnStatusException;

import tds.student.services.data.TestOpportunity;
import tds.student.web.TestManager;

/**
 * A service for verifying an {@link tds.student.services.data.TestOpportunity} can be moved to the "review" status.
 */
public interface ExamCompletionService {
  /**
   * Move an {@link tds.exam.Exam} to the "review" status, meaning a student has answered all the questions and is
   * ready to review the questions before submitting the exam for scoring.
   *
   * @param testOpportunity The {@link tds.student.services.data.TestOpportunity}
   * @param testManager     The {@link tds.student.web.TestManager} handling the
   *                        {@link tds.student.services.data.TestOpportunity}
   * @param statusCode      The code of the status to set the Exam to
   * @return A {@link AIR.Common.data.ResponseData} indicating success or failure
   * @throws ReturnStatusException In the event of a failure
   */
  ResponseData<String> updateStatusWithValidation(final TestOpportunity testOpportunity,
                                                  final TestManager testManager,
                                                  final String statusCode) throws ReturnStatusException;
}
