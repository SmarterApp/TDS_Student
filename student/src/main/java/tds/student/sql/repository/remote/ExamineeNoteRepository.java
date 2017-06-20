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

import tds.exam.ExamineeNote;
import tds.student.sql.data.OpportunityInstance;

/**
 * Interface for interacting with the methods in the TDS_ExamService that are responsible for handling
 * {@link tds.exam.ExamineeNote} data.
 */
public interface ExamineeNoteRepository {
    /**
     * Find an exam-level {@link tds.exam.ExamineeNote} for the specified
     * {@link tds.student.sql.data.OpportunityInstance}.
     *
     * @param opportunityInstance The {@link tds.student.sql.data.OpportunityInstance} for which the
     *                            {@link tds.exam.ExamineeNote} should be fetched
     * @return An {@link tds.exam.ExamineeNote} if it exists at the exam level; otherwise an empty/absent optional to
     * indicate no exam-level {@link tds.exam.ExamineeNote} was found.
     * @throws ReturnStatusException In the event of a failure
     */
    Optional<ExamineeNote> findNoteInExamContext(final OpportunityInstance opportunityInstance) throws ReturnStatusException;

    /**
     * Persist an exam-level {@link tds.exam.ExamineeNote} to the database.
     *
     * @param opportunityInstance The {@link tds.student.sql.data.OpportunityInstance} for which the
     *                            {@link tds.exam.ExamineeNote} should be persisted.
     * @param examineeNote        The {@link tds.exam.ExamineeNote} to persist
     * @throws ReturnStatusException In the event of a failure
     */
    void save(final OpportunityInstance opportunityInstance,
                final ExamineeNote examineeNote) throws ReturnStatusException;
}
