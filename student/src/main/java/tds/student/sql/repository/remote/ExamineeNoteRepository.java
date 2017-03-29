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
    void insert(final OpportunityInstance opportunityInstance,
                final ExamineeNote examineeNote) throws ReturnStatusException;
}
