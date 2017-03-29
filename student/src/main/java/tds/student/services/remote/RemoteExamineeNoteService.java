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
    void insertExamNote(final OpportunityInstance opportunityInstance,
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
    void insertItemNote(final OpportunityInstance opportunityInstance,
                        final long testeeKey,
                        final int position,
                        final String note) throws ReturnStatusException;
}
