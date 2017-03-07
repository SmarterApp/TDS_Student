package tds.student.sql.repository;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;

import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamApproval;
import tds.exam.ExamConfiguration;
import tds.exam.ExamPrintRequest;
import tds.exam.ExamSegment;
import tds.exam.OpenExamRequest;
import tds.exam.SegmentApprovalRequest;

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
   * @param examId the id of the {@link tds.exam.Exam}
   * @return the {@link tds.exam.ExamConfiguration} containing exam configuration metadata
   * @throws ReturnStatusException
   */
  Response<ExamConfiguration> startExam(final UUID examId) throws ReturnStatusException;

  /**
   * Creates a request to fetch {@link tds.exam.ExamSegment}s for an exam
   *
   * @param examId    the id of the exam
   * @param sessionId the id of the session the exam is a part of
   * @param browserId the id of the browser of the current session
   * @return a {@link tds.common.Response} containing the list of {@link tds.exam.ExamSegment}s for the exam
   * @throws ReturnStatusException
   */
  Response<List<ExamSegment>> findExamSegments(final UUID examId, final UUID sessionId, final UUID browserId) throws ReturnStatusException;

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
   * @param examId The id of the exam
   * @param segmentApprovalRequest A {@link tds.exam.SegmentApprovalRequest} containing request data
   * @throws ReturnStatusException
   */
  void waitForSegmentApproval(final UUID examId, final SegmentApprovalRequest segmentApprovalRequest) throws ReturnStatusException;

  /**
   * Creates a request to exit a segment
   *
   * @param examId The exam id of the {@link tds.exam.ExamSegment} to exit
   * @param segmentPosition The segment position of the {@link tds.exam.ExamSegment} to update
   * @throws ReturnStatusException
   */
  void exitSegment(final UUID examId, final int segmentPosition) throws ReturnStatusException;
}
