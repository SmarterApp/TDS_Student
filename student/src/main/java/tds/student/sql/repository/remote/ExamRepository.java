package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamApproval;
import tds.exam.ExamAssessmentMetadata;
import tds.exam.ExamConfiguration;
import tds.exam.ExamPrintRequest;
import tds.exam.ExamSegment;
import tds.exam.OpenExamRequest;
import tds.exam.SegmentApprovalRequest;
import tds.student.sql.data.OpportunityInstance;

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

  /**
   * Fetches the approval status of the current exam
   *
   * @param examId    the id of the exam
   * @param sessionId the id of the session the exam is a part of
   * @param browserId the id of the browser of the current session
   * @return the {@link tds.exam.ExamApproval} containing the status information
   * @throws ReturnStatusException
   */
  Response<ExamApproval> getApproval(final UUID examId, final UUID sessionId, final UUID browserId) throws ReturnStatusException;

  /**
   * Fetches the collection of approved {@link tds.exam.ExamAccommodation}s for an exam
   *
   * @param examId the id of the {@link tds.exam.Exam}
   * @return the list of approved {@link tds.exam.ExamAccommodation}s
   * @throws ReturnStatusException
   */
  List<ExamAccommodation> findApprovedAccommodations(final UUID examId) throws ReturnStatusException;

  /**
   * Creates a request for the exam service to approve {@link tds.exam.ExamAccommodation}s
   *
   * @param examId                       the id of the {@link tds.exam.Exam}
   * @param approveAccommodationsRequest the {@link tds.exam.ApproveAccommodationsRequest} containing approval request data
   * @throws ReturnStatusException
   */
  void approveAccommodations(final UUID examId, final ApproveAccommodationsRequest approveAccommodationsRequest) throws ReturnStatusException;

  /**
   * Creates a request to update the status of an exam
   *
   * @param examId the id of the {@link tds.exam.Exam}
   * @param status the status to update the exam to
   * @param reason the reason for the exam status update
   * @return An optional {@link tds.common.ValidationError}, if one occurs during the processing of the request
   * @throws ReturnStatusException
   */
  Optional<ValidationError> updateStatus(final UUID examId, final String status, final String reason) throws ReturnStatusException;

  /**
   * Creates a request to start an exam
   *
   * @param examId           the id of the {@link tds.exam.Exam}
   * @param browserUserAgent the browser user agent string
   * @return the {@link tds.exam.ExamConfiguration} containing exam configuration metadata
   * @throws ReturnStatusException
   */
  Response<ExamConfiguration> startExam(final UUID examId, final String browserUserAgent) throws ReturnStatusException;

  /**
   * Final review the {@link tds.exam.Exam} to verify it is complete and ready for transmission to downstream systems
   * prior to submitting it.
   *
   * @param opportunityInstance The {@link tds.student.sql.data.OpportunityInstance} representing the exam to review
   * @return A {@link tds.common.ValidationError} if the review process fails; otherwise {@code Optional.empty}, which
   * indicates success
   * @throws ReturnStatusException
   */
  Optional<ValidationError> reviewExam(final OpportunityInstance opportunityInstance) throws ReturnStatusException, IOException;

  /**
   * Creates a request to fetch {@link tds.exam.ExamSegment}s for an exam
   *
   * @param examId    the id of the exam
   * @param sessionId the id of the session the exam is a part of
   * @param browserId the id of the browser of the current session
   * @return a {@link tds.common.Response} containing the list of {@link tds.exam.ExamSegment}s for the exam
   * @throws ReturnStatusException
   */
  List<ExamSegment> findExamSegments(final UUID examId, final UUID sessionId, final UUID browserId) throws ReturnStatusException;

  /**
   * Creats an {@link tds.exam.ExamPrintRequest} to print or emboss an item, page, or passage
   *
   * @param examPrintRequest The {@link tds.exam.ExamPrintRequest} containing print data
   * @throws ReturnStatusException
   */
  void createPrintRequest(final ExamPrintRequest examPrintRequest) throws ReturnStatusException;

  /**
   * Creates a segment approval request for an exam segment
   *
   * @param examId                 The id of the exam
   * @param segmentApprovalRequest A {@link tds.exam.SegmentApprovalRequest} containing request data
   * @throws TDS.Shared.Exceptions.ReturnStatusException
   */
  void waitForSegmentApproval(final UUID examId, final SegmentApprovalRequest segmentApprovalRequest) throws ReturnStatusException;

  /**
   * Finds the list of exam assessments available for a student and session.
   *
   * @param studentId  The id of the student to fetch {@link tds.exam.ExamAssessmentMetadata}s for
   * @param sessionId  The id current session
   * @param grade      The assessment grades to fetch
   * @return A list of {@link tds.exam.ExamAssessmentMetadata}, containing various metadata pertaining to the assessment and exams.
   * @throws TDS.Shared.Exceptions.ReturnStatusException
   */
  List<ExamAssessmentMetadata> findExamAssessmentInfo(final long studentId, final UUID sessionId, final String grade) throws ReturnStatusException;
}
