package tds.student.sql.abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.common.Response;
import tds.exam.Exam;
import tds.exam.OpenExamRequest;

/**
 * Repository to interact with exam data
 */
public interface ExamRepository {

  /**
   * Opens an exam
   *
   * @param openExamRequest the {@link tds.exam.OpenExamRequest} containing the required exam information to open an exam
   * @return {@link tds.common.Response} with an {@link tds.exam.Exam} if successful otherwise will have a {@link tds.common.ValidationError}
   * @throws ReturnStatusException if there is an unexpected response from the call
   */
  Response<Exam> openExam(final OpenExamRequest openExamRequest) throws ReturnStatusException;
}
