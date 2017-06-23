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

import java.util.UUID;

/**
 * Repository to interact with exam segment data
 */
public interface ExamSegmentRepository {

    /**
     * Creates a request to exit a segment
     *
     * @param examId The exam id of the {@link tds.exam.ExamSegment} to exit
     * @param segmentPosition The segment position of the {@link tds.exam.ExamSegment} to update
     * @throws ReturnStatusException
     */
    void exitSegment(final UUID examId, final int segmentPosition) throws ReturnStatusException;

    /**
     * Checks to see if all the {@link tds.exam.ExamSegment}s for the {@link tds.exam.Exam} are satisfied
     *
     * @param examId The id of the exam
     * @return {@code true} if the exam segments are all satisfied, false otherwise
     */
    boolean checkSegmentsSatisfied(final UUID examId) throws ReturnStatusException;
}
