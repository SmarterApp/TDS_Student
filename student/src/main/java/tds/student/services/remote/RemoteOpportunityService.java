package tds.student.services.remote;

import TDS.Shared.Browser.BrowserInfo;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.OpenExamRequest;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.data.ApprovalInfo;
import tds.student.sql.abstractions.ExamRepository;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunitySegment;
import tds.student.sql.data.OpportunityStatus;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.OpportunityStatusExtensions;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestSegment;
import tds.student.sql.data.TestSelection;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;

@Service("integrationOpportunityService")
@Scope("prototype")
public class RemoteOpportunityService implements IOpportunityService {
  private final IOpportunityService legacyOpportunityService;
  private final boolean isRemoteExamCallsEnabled;
  private final boolean isLegacyCallsEnabled;
  private final ExamRepository examRepository;

  @Autowired
  public RemoteOpportunityService(
    @Qualifier("legacyOpportunityService") IOpportunityService legacyOpportunityService,
    @Value("${tds.exam.remote.enabled}") Boolean remoteExamCallsEnabled,
    @Value("${tds.exam.legacy.enabled}") Boolean legacyCallsEnabled,
    ExamRepository examRepository) {

    if(!remoteExamCallsEnabled && !legacyCallsEnabled) {
      throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration");
    }

    this.isRemoteExamCallsEnabled = remoteExamCallsEnabled;
    this.legacyOpportunityService = legacyOpportunityService;
    this.isLegacyCallsEnabled = legacyCallsEnabled;
    this.examRepository = examRepository;
  }

  @Override
  public List<TestSelection> getEligibleTests(final Testee testee, final TestSession session, final String grade, final BrowserInfo browserInfo) throws ReturnStatusException {
    return legacyOpportunityService.getEligibleTests(testee, session, grade, browserInfo);
  }

  @Override
  public OpportunityInfo openTest(final Testee testee, final TestSession session, final String testKey) throws ReturnStatusException {
    OpportunityInfo legacyOpportunityInfo = null;

    if(isLegacyCallsEnabled) {
      legacyOpportunityInfo = legacyOpportunityService.openTest(testee, session, testKey);
    }

    //This isn't ideal, but due to the way the progman properties are loaded within the system this lives within the service rather than the callers.
    if (!isRemoteExamCallsEnabled) {
      return legacyOpportunityInfo;
    }

    OpenExamRequest openExamRequest = new OpenExamRequest.Builder()
      .withAssessmentKey(testKey)
      .withSessionId(session.getKey())
      .withBrowserId(UUID.randomUUID())
      .withStudentId(testee.getKey())
      .build();

    Response<Exam> response = examRepository.openExam(openExamRequest);

    if (!response.hasError() && !response.getData().isPresent()) {
      throw new ReturnStatusException("Invalid response from the exam service");
    }

    if (response.getError().isPresent()) {
      ValidationError validationError = response.getError().get();
      String errorMessage = validationError.getTranslatedMessage().isPresent()
        ? validationError.getTranslatedMessage().get()
        : validationError.getMessage();

      throw new ReturnStatusException(errorMessage);
    }

    if (!response.getData().isPresent()) {
      throw new ReturnStatusException("Invalid response from the exam service");
    }

    //By the time we reach this point data will always be present
    Exam exam = response.getData().get();

    OpportunityInfo remoteOpportunityInfo = new OpportunityInfo();
    remoteOpportunityInfo.setBrowserKey(exam.getBrowserId());
    remoteOpportunityInfo.setOppKey(exam.getId());
    remoteOpportunityInfo.setStatus(OpportunityStatusExtensions.parseExamStatus(exam.getStatus().getCode()));

    return remoteOpportunityInfo;
  }

  @Override
  public OpportunityStatus getStatus(final OpportunityInstance oppInstance) throws ReturnStatusException {
    return legacyOpportunityService.getStatus(oppInstance);
  }

  @Override
  public boolean setStatus(final OpportunityInstance oppInstance, final OpportunityStatusChange statusChange) throws ReturnStatusException {
    return legacyOpportunityService.setStatus(oppInstance, statusChange);
  }

  @Override
  public ApprovalInfo checkTestApproval(final OpportunityInstance oppInstance) throws ReturnStatusException {
    return legacyOpportunityService.checkTestApproval(oppInstance);
  }

  @Override
  public ApprovalInfo checkSegmentApproval(final OpportunityInstance oppInstance) throws ReturnStatusException {
    return legacyOpportunityService.checkSegmentApproval(oppInstance);
  }

  @Override
  public void denyApproval(final OpportunityInstance oppInstance) throws ReturnStatusException {
    legacyOpportunityService.denyApproval(oppInstance);
  }

  @Override
  public TestConfig startTest(final OpportunityInstance oppInstance, final String testKey, final List<String> formKeys) throws ReturnStatusException {
    return legacyOpportunityService.startTest(oppInstance, testKey, formKeys);
  }

  @Override
  public OpportunitySegment.OpportunitySegments getSegments(final OpportunityInstance oppInstance, final boolean validate) throws ReturnStatusException {
    return legacyOpportunityService.getSegments(oppInstance, validate);
  }

  @Override
  public void waitForSegment(final OpportunityInstance oppInstance, final int segmentPosition, final TestSegment.TestSegmentApproval segmentApproval) throws ReturnStatusException {
    legacyOpportunityService.waitForSegment(oppInstance, segmentPosition, segmentApproval);
  }

  @Override
  public void exitSegment(final OpportunityInstance oppInstance, final int segmentPosition) throws ReturnStatusException {
    legacyOpportunityService.exitSegment(oppInstance, segmentPosition);
  }
}
