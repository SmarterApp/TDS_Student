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

import tds.student.sql.data.OpportunityInstance;

/**
 * Interface for interacting with {@link tds.exam.ExamineeNote} data from a remote source (e.g. the TDS_ExamService).
 */
public interface RemoteExamineeNoteService {
    /**
     * Find the exam-level {@link tds.exam.ExamineeNote} for the specified
     * {@link tds.student.sql.data.OpportunityInstance}
     *
     * @param opportunityInstance The {@link tds.student.sql.data.OpportunityInstance} for which the exam-level comment/
     *                            note should be fetched
     * @return The comment/note text for the exam-level {@link tds.exam.ExamineeNote}
     * @throws TDS.Shared.Exceptions.ReturnStatusException in the event of a failure
     */
    String findExamNote(final OpportunityInstance opportunityInstance) throws ReturnStatusException;

    /**
     * Persist an exam-level {@link tds.exam.ExamineeNote} to the database
     *
     * @param opportunityInstance The {@link tds.student.sql.data.OpportunityInstance} for which the exam-level comment/
     *                            note should be saved
     * @param testeeKey           The RTS identifier of the student taking this
     *                            {@link tds.student.sql.data.OpportunityInstance}
     * @param note                The note text to save
     * @throws TDS.Shared.Exceptions.ReturnStatusException in the event of a failure
     */
    void saveExamNote(final OpportunityInstance opportunityInstance,
                      final long testeeKey,
                      final String note) throws ReturnStatusException;

    /**
     * Persist an item-level note {@link tds.exam.ExamineeNote} to the database
     *
     * @param opportunityInstance The {@link tds.student.sql.data.OpportunityInstance} for which the exam-level comment/
     *                            note should be saved
     * @param testeeKey           The RTS identifier of the student taking this
     *                            {@link tds.student.sql.data.OpportunityInstance}
     * @param position            The position of the {@link tds.exam.ExamItem} where the note text was created
     * @param note                The note text provided by the user
     * @throws ReturnStatusException In the event of a failure
     */
    void saveItemNote(final OpportunityInstance opportunityInstance,
                      final long testeeKey,
                      final int position,
                      final String note) throws ReturnStatusException;
}
