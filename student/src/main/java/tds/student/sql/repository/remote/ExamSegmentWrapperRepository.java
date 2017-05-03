package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;

import java.util.List;
import java.util.UUID;

import tds.exam.wrapper.ExamSegmentWrapper;

public interface ExamSegmentWrapperRepository {
  List<ExamSegmentWrapper> findAllExamSegmentWrappersForExam(final UUID examId) throws ReturnStatusException;

  Optional<ExamSegmentWrapper> findExamSegmentWrappersForExamAndPagePosition(final UUID examId, int pagePosition) throws ReturnStatusException;
}
