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
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;

import java.util.List;
import java.util.UUID;

/**
 * Implementations of this interface are responsible for updating exam item scores.
 */
public interface ItemScoringRepository {

    /**
     * Update and score the given item responses.
     *
     * @param examId            The exam id
     * @param sessionId         The session id
     * @param browserId         The browser id
     * @param clientName        The exam client name
     * @param pageDuration      The page duration
     * @param responseUpdates   The student responses
     * @return The response update status values for the responses
     * @throws ReturnStatusException On an unexpected response
     */
    List<ItemResponseUpdateStatus> updateResponses(final UUID examId,
                                                   final UUID sessionId,
                                                   final UUID browserId,
                                                   final String clientName,
                                                   final Float pageDuration,
                                                   final List<ItemResponseUpdate> responseUpdates) throws ReturnStatusException;
}
