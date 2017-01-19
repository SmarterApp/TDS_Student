package tds.student.sql.abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.common.Response;
import tds.exam.Exam;
import tds.exam.OpenExamRequest;

/**
 * Repository to interact with exam data
 */
public interface ExamRepository {
  Response<Exam> openExam(OpenExamRequest openExamRequest) throws ReturnStatusException;
}
