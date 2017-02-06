package tds.student.sql.abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.UUID;

import tds.common.Response;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.OpenExamRequest;

/**
 * Repository to interact with exam data
 */
public interface ExamRepository {
  Response<Exam> openExam(final OpenExamRequest openExamRequest) throws ReturnStatusException;
  void approveAccommodations(final UUID examId, final ApproveAccommodationsRequest approveAccommodationsRequest) throws ReturnStatusException;
}
