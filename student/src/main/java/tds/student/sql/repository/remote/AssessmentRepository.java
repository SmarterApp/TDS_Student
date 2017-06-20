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

import tds.assessment.Assessment;

/**
 * Repository to interact with Assessment data
 */
public interface AssessmentRepository {
  /**
   * Retrieves an {@link tds.assessment.Assessment} from the assessment service by the assessment key
   *
   * @param clientName the current envrionment's client name
   * @param key        the key of the {@link tds.assessment.Assessment}
   * @return the fully populated {@link tds.assessment.Assessment}
   * @throws ReturnStatusException
   */
  Assessment findAssessment(final String clientName, final String key) throws ReturnStatusException;
}
