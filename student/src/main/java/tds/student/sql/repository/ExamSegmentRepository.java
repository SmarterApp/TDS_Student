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
}
