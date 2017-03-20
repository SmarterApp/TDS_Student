package tds.student.sql.repository;

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
