package tds.student.services.remote;

import TDS.Shared.Browser.BrowserInfo;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import tds.exam.ExamApproval;
import tds.exam.ExamConfiguration;
import tds.exam.ExamStatusCode;
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
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestSegment;
import tds.student.sql.data.TestSelection;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;

@Service("integrationOpportunityService")
@Scope("prototype")
public class RemoteOpportunityService implements IOpportunityService {
  private static final Logger log = LoggerFactory.getLogger(RemoteOpportunityService.class);
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
    OpportunityInfo opportunityInfo = new OpportunityInfo();

    if(isLegacyCallsEnabled) {
      opportunityInfo = legacyOpportunityService.openTest(testee, session, testKey);
    }

    //This isn't ideal, but due to the way the progman properties are loaded within the system this lives within the service rather than the callers.
    if (!isRemoteExamCallsEnabled) {
      return opportunityInfo;
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
    opportunityInfo.setExamBrowserKey(exam.getBrowserId());
    opportunityInfo.setExamId(exam.getId());
    opportunityInfo.setExamStatus(OpportunityStatusExtensions.parseExamStatus(exam.getStatus().getCode()));
    opportunityInfo.setExamClientName(exam.getClientName());

    return opportunityInfo;
  }

  @Override
  public OpportunityStatus getStatus(final OpportunityInstance oppInstance) throws ReturnStatusException {
    OpportunityStatus status = null;

    if(isLegacyCallsEnabled) {
      status = legacyOpportunityService.getStatus(oppInstance);
    }

    //This isn't ideal, but due to the way the progman properties are loaded within the system this lives within the service rather than the callers.
    if (!isRemoteExamCallsEnabled) {
      return status;
    }

    Response<ExamApproval> response = examRepository.getApproval(oppInstance.getExamId(), oppInstance.getSessionKey(),
        oppInstance.getExamBrowserKey());

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

    ExamApproval examApproval = response.getData().get();
    status = new OpportunityStatus();
    status.setStatus(OpportunityStatusExtensions.parseExamStatus(examApproval.getExamStatusCode()));

    return status;
  }

  @Override
  public boolean setStatus(final OpportunityInstance oppInstance, final OpportunityStatusChange statusChange) throws ReturnStatusException {
    boolean isApproved = false;
    ReturnStatus returnStatus = null;
    
    if (isLegacyCallsEnabled) {
      isApproved = legacyOpportunityService.setStatus(oppInstance, statusChange);
    }
  
    if (!isRemoteExamCallsEnabled) {
      return isApproved;
    }
    
    Optional<ValidationError> maybeError = examRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name(), statusChange.getReason());
    
    if (!statusChange.isCheckReturnStatus()) {
      return true;
    }
    
    if (!maybeError.isPresent()) {
      return true;
    }
  
    ValidationError error = maybeError.get();
    returnStatus = new ReturnStatus(error.getCode(), error.getMessage());
    
    if (ExamStatusCode.STATUS_FAILED.equalsIgnoreCase (maybeError.get().getCode())) {
      throw new ReturnStatusException (returnStatus);
    }
    /* OpportunityService - We can skip line [234-237] as trying to update to an invalid status should result in a ValidationError generated
       by the ExamService. */
    
    log.warn("Error setting exam status for exam id {}: Failed to set status to '{}' - {}",
      oppInstance.getExamId(), statusChange.getStatus(), returnStatus.getReason());
    isApproved = false;
    
    return isApproved;
  }

  @Override
  public ApprovalInfo checkTestApproval(final OpportunityInstance oppInstance) throws ReturnStatusException {
    ApprovalInfo approvalInfo = null;

    if(isLegacyCallsEnabled) {
      approvalInfo = legacyOpportunityService.checkTestApproval(oppInstance);
    }

    if (!isRemoteExamCallsEnabled) {
      return approvalInfo;
    }

    return new ApprovalInfo(getStatus(oppInstance));
  }

  @Override
  public ApprovalInfo checkSegmentApproval(final OpportunityInstance oppInstance) throws ReturnStatusException {
    return legacyOpportunityService.checkSegmentApproval(oppInstance);
  }

  @Override
  public void denyApproval(final OpportunityInstance oppInstance) throws ReturnStatusException {
  
    if(isLegacyCallsEnabled) {
      legacyOpportunityService.denyApproval(oppInstance);
    }

    if (!isRemoteExamCallsEnabled) {
      return;
    }
    
    OpportunityStatus opportunityStatus = getStatus(oppInstance);
  
    /* OpportunityService - Conditional on line [257] */
    if (opportunityStatus.getStatus() == OpportunityStatusType.Paused) {
      return;
    }
    
    setStatus (oppInstance, new OpportunityStatusChange (OpportunityStatusType.Pending, true, ExamStatusCode.STATUS_DENIED));
  }

  @Override
  public TestConfig startTest(final OpportunityInstance oppInstance, final String testKey, final List<String> formKeys) throws ReturnStatusException {
    TestConfig testConfig = null;
    
    if(isLegacyCallsEnabled) {
      testConfig = legacyOpportunityService.startTest(oppInstance, testKey, formKeys);
    }
  
    if (!isRemoteExamCallsEnabled) {
      return testConfig;
    }
  
    
    /* Note that the formKeys argument can be ignored - it is an unused functionality */
    Response<ExamConfiguration> response = examRepository.startExam(oppInstance.getExamId());
  
    if (response.getError().isPresent()) {
      ValidationError validationError = response.getError().get();
      String errorMessage = validationError.getTranslatedMessage().isPresent()
        ? validationError.getTranslatedMessage().get()
        : validationError.getMessage();
    
      throw new ReturnStatusException(errorMessage);
    }
    
    if (!response.getData().isPresent()) {
      throw new ReturnStatusException(String.format("Invalid response from the exam service when trying to start exam %s", oppInstance.getExamId()));
    }

    ExamConfiguration examConfiguration = response.getData().get();
    
    /* OpportunityService - Conditional at line 288 */
    if (!examConfiguration.getStatus().equals(ExamStatusCode.STATUS_STARTED)) {
      throw new ReturnStatusException("Failed to start the exam.");
    }
  
    testConfig = mapExamConfigurationToTestConfig(examConfiguration);
    
    return testConfig;
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
  
  private static TestConfig mapExamConfigurationToTestConfig(ExamConfiguration examConfiguration) {
    Exam exam = examConfiguration.getExam();
    TestConfig testConfig = new TestConfig();
    testConfig.setStatus(OpportunityStatusExtensions.parseExamStatus(examConfiguration.getStatus()));
    testConfig.setRestart(exam.getRestartsAndResumptions());
    testConfig.setTestLength(examConfiguration.getTestLength());
    testConfig.setStartPosition(examConfiguration.getStartPosition());
    testConfig.setContentLoadTimeout(examConfiguration.getContentLoadTimeoutMinutes());
    testConfig.setInterfaceTimeout(examConfiguration.getInterfaceTimeoutMinutes());
    testConfig.setRequestInterfaceTimeout(examConfiguration.getRequestInterfaceTimeoutMinutes());
    testConfig.setOppRestartMins(examConfiguration.getExamRestartWindowMinutes());
    testConfig.setPrefetch(examConfiguration.getPrefetch());
    testConfig.setScoreByTDS(false);
    testConfig.setValidateCompleteness(examConfiguration.isValidateCompleteness());
    //TODO: set MSB flag
    return testConfig;
  }
}
