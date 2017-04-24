package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;

import tds.exam.ExamPage;
import tds.student.sql.data.OpportunityInstance;

/**
 * Repository for interacting with exam pages in Exam Service
 */
public interface ExamPageRepository {
    /**
     * Find an {@link tds.exam.ExamPage} with its associated {@link tds.exam.ExamItem}s for the specified
     * {@link tds.exam.Exam} and page position.
     *
     * @param opportunityInstance The {@link tds.student.sql.data.OpportunityInstance} of the student taking a
     *                            particular {@link tds.exam.Exam}
     * @param position            The position/number of the {@link tds.exam.ExamPage} to fetch
     * @return An {@link tds.exam.ExamPage} with its constituent {@link tds.exam.ExamItem}s
     * @throws ReturnStatusException In the event of a failure fetch the {@link tds.exam.ExamPage}
     */
    ExamPage findPageWithItems(final OpportunityInstance opportunityInstance, final int position) throws ReturnStatusException;

    /**
     * Find all {@link tds.exam.ExamPage}s and their associated {@link tds.exam.ExamItem}s for the specified
     * {@link tds.exam.Exam}.
     *
     * @param opportunityInstance The {@link tds.student.sql.data.OpportunityInstance} of the student taking a
     *                            particular {@link tds.exam.Exam}
     * @return A collection of {@link tds.exam.ExamPage}s, each with its constituent {@link tds.exam.ExamItem}s
     * @throws ReturnStatusException In the event of a failure fetch the {@link tds.exam.ExamPage}s
     */
    List<ExamPage> findAllPagesWithItems(final OpportunityInstance opportunityInstance) throws ReturnStatusException;
}
