/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.student.services.remote;

import TDS.Shared.Browser.BrowserAction;
import TDS.Shared.Browser.BrowserInfo;
import TDS.Shared.Browser.BrowserRule;
import TDS.Shared.Browser.BrowserValidation;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamAssessmentMetadata;
import tds.exam.ExamConfiguration;
import tds.exam.ExamSegment;
import tds.exam.ExamStatusCode;
import tds.exam.OpenExamRequest;
import tds.exam.SegmentApprovalRequest;
import tds.student.performance.dao.TestOpportunityExamMapDao;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.data.ApprovalInfo;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunitySegment;
import tds.student.sql.data.OpportunityStatus;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestSegment;
import tds.student.sql.data.TestSelection;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;
import tds.student.sql.repository.ConfigRepository;
import tds.student.sql.repository.remote.ExamRepository;
import tds.student.sql.repository.remote.ExamSegmentRepository;

@Service("integrationOpportunityService")
@Scope("prototype")
public class RemoteOpportunityService implements IOpportunityService {
  private static final Logger log = LoggerFactory.getLogger(RemoteOpportunityService.class);
  private final IOpportunityService legacyOpportunityService;
  private final boolean isRemoteExamCallsEnabled;
  private final boolean isLegacyCallsEnabled;
  private final ExamRepository examRepository;
  private final ExamSegmentRepository examSegmentRepository;
  private final TestOpportunityExamMapDao testOpportunityExamMapDao;
  private final ConfigRepository configRepository;

  @Autowired
  public RemoteOpportunityService(
    @Qualifier("legacyOpportunityService") final IOpportunityService legacyOpportunityService,
    @Value("${tds.exam.remote.enabled}") final Boolean remoteExamCallsEnabled,
    @Value("${tds.exam.legacy.enabled}") final Boolean legacyCallsEnabled,
    final ExamRepository examRepository,
    final ExamSegmentRepository examSegmentRepository,
    final TestOpportunityExamMapDao testOpportunityExamMapDao,
    final ConfigRepository configRepository) {

    if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
      throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration");
    }

    this.isRemoteExamCallsEnabled = remoteExamCallsEnabled;
    this.legacyOpportunityService = legacyOpportunityService;
    this.isLegacyCallsEnabled = legacyCallsEnabled;
    this.examRepository = examRepository;
    this.testOpportunityExamMapDao = testOpportunityExamMapDao;
    this.examSegmentRepository = examSegmentRepository;
    this.configRepository = configRepository;
  }

  @Override
  public List<TestSelection> getEligibleTests(final Testee testee, final TestSession session, final String grade, final BrowserInfo browserInfo) throws ReturnStatusException {
    List<TestSelection> testSelections = new ArrayList<>();

    if (isLegacyCallsEnabled) {
      testSelections = legacyOpportunityService.getEligibleTests(testee, session, grade, browserInfo);
    }

    if (!isRemoteExamCallsEnabled) {
      return testSelections;
    }

    List<ExamAssessmentMetadata> assessmentMetadata = examRepository.findExamAssessmentInfo(testee.getKey(), session.getKey(), grade);

    return mapAssessmentMetadataToTestSelections(assessmentMetadata, browserInfo);
  }

  @Override
  public OpportunityInfo openTest(final Testee testee, final TestSession session, final String testKey) throws ReturnStatusException {
    OpportunityInfo opportunityInfo = new OpportunityInfo();

    if (isLegacyCallsEnabled) {
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
    opportunityInfo.setExamStatus(ExamStatusMapper.parseExamStatus(exam.getStatus().getCode()));
    opportunityInfo.setExamClientName(exam.getClientName());

    //If we are calling both legacy and remote services, then we need to store a map from test opportunity id to exam id
    if (isRemoteExamCallsEnabled && isLegacyCallsEnabled) {
      testOpportunityExamMapDao.insert(opportunityInfo.getOppKey(), exam.getId());
    }

    return opportunityInfo;
  }

  @Override
  public OpportunityStatus getStatus(final OpportunityInstance oppInstance) throws ReturnStatusException {
    OpportunityStatus status = null;

    if (isLegacyCallsEnabled) {
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
    status.setStatus(ExamStatusMapper.parseExamStatus(examApproval.getExamStatusCode()));
    status.setComment(examApproval.getStatusChangeReason());

    return status;
  }

  @Override
  public boolean setStatus(final OpportunityInstance oppInstance, final OpportunityStatusChange statusChange) throws ReturnStatusException {
    boolean isApproved = false;
    ReturnStatus returnStatus;

    if (isLegacyCallsEnabled) {
      isApproved = legacyOpportunityService.setStatus(oppInstance, statusChange);
    }

    if (!isRemoteExamCallsEnabled) {
      return isApproved;
    }

    Optional<ValidationError> maybeError = examRepository.updateStatus(oppInstance.getExamId(), statusChange.getStatus().name().toLowerCase(), statusChange.getReason());

    if (!statusChange.isCheckReturnStatus()) {
      return true;
    }

    if (!maybeError.isPresent()) {
      return true;
    }

    ValidationError error = maybeError.get();
    returnStatus = new ReturnStatus(error.getCode(), error.getMessage());

    if (ExamStatusCode.STATUS_FAILED.equalsIgnoreCase(maybeError.get().getCode())) {
      log.warn("Error setting exam status for exam id {}: Failed to set status to '{}' - {}",
          oppInstance.getExamId(), statusChange.getStatus(), returnStatus.getReason());

      throw new ReturnStatusException(returnStatus);
    }
    /* OpportunityService - We can skip line [234-237] as trying to update to an invalid status should result in a ValidationError generated
       by the ExamService. */

    return false;
  }

  @Override
  public ApprovalInfo checkTestApproval(final OpportunityInstance oppInstance) throws ReturnStatusException {
    ApprovalInfo approvalInfo = null;

    if (isLegacyCallsEnabled) {
      approvalInfo = legacyOpportunityService.checkTestApproval(oppInstance);
    }

    if (!isRemoteExamCallsEnabled) {
      return approvalInfo;
    }

    return new ApprovalInfo(getStatus(oppInstance));
  }

  @Override
  public ApprovalInfo checkSegmentApproval(final OpportunityInstance oppInstance) throws ReturnStatusException {
    ApprovalInfo approvalInfo = null;

    if (isLegacyCallsEnabled) {
      approvalInfo = legacyOpportunityService.checkSegmentApproval(oppInstance);
    } else {
      approvalInfo = checkTestApproval(oppInstance);
    }

    if (!isRemoteExamCallsEnabled) {
      return approvalInfo;
    }

    if (ExamStatusCode.STATUS_APPROVED.equalsIgnoreCase(approvalInfo.getStatus().name())) {
      examRepository.updateStatus(oppInstance.getExamId(), ExamStatusCode.STATUS_STARTED, "segment");
    }

    return approvalInfo;
  }

  @Override
  public void denyApproval(final OpportunityInstance oppInstance) throws ReturnStatusException {
    // Since setStatus checks and calls the legacy and remote as needed, we don't need that logic here
    //  otherwise the legacy service will be called twice

    OpportunityStatus opportunityStatus = getStatus(oppInstance);
  
    /* OpportunityService - Conditional on line [257] */
    if (opportunityStatus.getStatus() == OpportunityStatusType.Paused) {
      return;
    }

    setStatus(oppInstance, new OpportunityStatusChange(OpportunityStatusType.Pending, true, ExamStatusCode.STATUS_DENIED));
  }

  @Override
  public TestConfig startTest(final OpportunityInstance oppInstance, final String testKey, final List<String> formKeys) throws ReturnStatusException {
    TestConfig testConfig = null;

    if (isLegacyCallsEnabled) {
      testConfig = legacyOpportunityService.startTest(oppInstance, testKey, formKeys);
    }

    if (!isRemoteExamCallsEnabled) {
      return testConfig;
    }

    final String unquotedBrowserUserAgent = oppInstance.getBrowserUserAgent().replaceAll("^\"|\"$", "");
    /* Note that the formKeys argument can be ignored - it is an unused functionality */
    final Response<ExamConfiguration> response = examRepository.startExam(oppInstance.getExamId(), unquotedBrowserUserAgent);

    if (response.getError().isPresent()) {
      final ValidationError validationError = response.getError().get();
      final String errorMessage = validationError.getTranslatedMessage().isPresent()
        ? validationError.getTranslatedMessage().get()
        : validationError.getMessage();

      throw new ReturnStatusException(errorMessage);
    }

    if (!response.getData().isPresent()) {
      throw new ReturnStatusException(String.format("Invalid response from the exam service when trying to start exam %s", oppInstance.getExamId()));
    }

    final ExamConfiguration examConfiguration = response.getData().get();

    return mapExamConfigurationToTestConfig(examConfiguration);
  }

  @Override
  public OpportunitySegment.OpportunitySegments getSegments(final OpportunityInstance oppInstance, final boolean validate) throws ReturnStatusException {
    /* Note, the "validate" argument is not used and is always TRUE */
    OpportunitySegment.OpportunitySegments opportunitySegments = null;

    if (isLegacyCallsEnabled) {
      opportunitySegments = legacyOpportunityService.getSegments(oppInstance, validate);
    }

    if (!isRemoteExamCallsEnabled) {
      return opportunitySegments;
    }

    final List<ExamSegment> examSegments = examRepository.findExamSegments(oppInstance.getExamId(),
      oppInstance.getSessionKey(), oppInstance.getExamBrowserKey());

    opportunitySegments = mapExamSegmentsToOpportunitySegments(examSegments);

    return opportunitySegments;
  }

  @Override
  public void waitForSegment(final OpportunityInstance oppInstance, final int segmentPosition,
                             final TestSegment.TestSegmentApproval segmentApproval) throws ReturnStatusException {
    if (isLegacyCallsEnabled) {
      legacyOpportunityService.waitForSegment(oppInstance, segmentPosition, segmentApproval);
    }

    if (!isRemoteExamCallsEnabled) {
      return;
    }

    boolean isEntryApproval = (segmentApproval == TestSegment.TestSegmentApproval.Entry);

    SegmentApprovalRequest request = new SegmentApprovalRequest(oppInstance.getSessionKey(), oppInstance.getExamBrowserKey(),
      segmentPosition, isEntryApproval);

    examRepository.waitForSegmentApproval(oppInstance.getExamId(), request);
  }

  @Override
  public void exitSegment(final OpportunityInstance oppInstance, final int segmentPosition) throws ReturnStatusException {
    if (isLegacyCallsEnabled) {
      legacyOpportunityService.exitSegment(oppInstance, segmentPosition);
    }

    if (!isRemoteExamCallsEnabled) {
      return;
    }

    examSegmentRepository.exitSegment(oppInstance.getExamId(), segmentPosition);
  }

  @Override
  public int getAttemptNumber(final OpportunityInstance opportunityInstance) throws ReturnStatusException {
    int attemptNumber = 0;

    if (isLegacyCallsEnabled) {
      attemptNumber = legacyOpportunityService.getAttemptNumber(opportunityInstance);
    }

    if (!isRemoteExamCallsEnabled) {
      return attemptNumber;
    }

    return examRepository.getExamById(opportunityInstance.getExamId()).getAttempts();
  }

  private static TestConfig mapExamConfigurationToTestConfig(ExamConfiguration examConfiguration) {
    Exam exam = examConfiguration.getExam();
    TestConfig testConfig = new TestConfig();
    testConfig.setStatus(ExamStatusMapper.parseExamStatus(examConfiguration.getStatus()));
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

  private static OpportunitySegment.OpportunitySegments mapExamSegmentsToOpportunitySegments(List<ExamSegment> examSegments) {
    OpportunitySegment.OpportunitySegments opportunitySegments = new OpportunitySegment().new OpportunitySegments();

    for (ExamSegment examSegment : examSegments) {
      OpportunitySegment oppSegment = new OpportunitySegment();
      oppSegment.setFormID(examSegment.getFormId());
      oppSegment.setFormKey(examSegment.getFormKey());
      oppSegment.setFtItems(String.valueOf(examSegment.getFieldTestItemCount()));
      oppSegment.setId(examSegment.getSegmentId());
      oppSegment.setKey(examSegment.getSegmentKey());
      oppSegment.setPosition(examSegment.getSegmentPosition());
      oppSegment.setIsPermeable(examSegment.isPermeable() ? 1 : -1);
      oppSegment.setRestorePermOn(examSegment.getRestorePermeableCondition());
      opportunitySegments.add(oppSegment);
    }

    return opportunitySegments;
  }

  private List<TestSelection> mapAssessmentMetadataToTestSelections(final List<ExamAssessmentMetadata> assessmenMetadata,
                                                                    final BrowserInfo browserInfo) throws ReturnStatusException {
    List<TestSelection> selections = new ArrayList<>();

    for (ExamAssessmentMetadata metadata : assessmenMetadata) {
      TestSelection selection = new TestSelection();
      selection.SetReturnStatus(new ReturnStatus(metadata.getStatus(), metadata.getDeniedReason()));
      selection.setTestKey(metadata.getAssessmentKey());
      selection.setTestID(metadata.getAssessmentId());
      selection.setOpportunity(metadata.getAttempt());
      selection.setMode("online");
      selection.setDisplayName(metadata.getAssessmentLabel());
      selection.setMaxOpportunities(metadata.getMaxAttempts());
      selection.setSortOrder(0); // not used by UI
      selection.setSubject(metadata.getSubject());
      selection.setGrade(metadata.getGrade());

      validateBrowserInfo(browserInfo, selection);

      selections.add(selection);
    }

    return selections;
  }

  private void validateBrowserInfo(final BrowserInfo browserInfo, final TestSelection selection) throws ReturnStatusException {

    if (browserInfo == null
        || selection.getTestStatus() == TestSelection.Status.Disabled
        || selection.getTestStatus() == TestSelection.Status.Hidden) {
      return;
    }

    BrowserValidation browserValidation = new BrowserValidation();

    for (BrowserRule rule : configRepository.getBrowserTestRules(selection.getTestID())) {
      browserValidation.AddRule (rule);
    }

    if (browserValidation.GetRules() != null && !browserValidation.GetRules().isEmpty()) {
      // get the rule that matches our current browser info
      BrowserRule browserRule = browserValidation.FindRule(browserInfo);

      if (browserRule == null || browserRule.getAction() == BrowserAction.Deny) {
        selection.setTestStatus(TestSelection.Status.Disabled);

        if (browserRule == null || StringUtils.isEmpty (browserRule.getMessageKey ()))
        {
          selection.setReasonKey("BrowserDeniedTest");
        }
        else
        {
          selection.setReasonKey(browserRule.getMessageKey ());
        }
      } else if (browserRule.getAction() == BrowserAction.Warn) {
        if (StringUtils.isEmpty (browserRule.getMessageKey ()))
        {
          selection.setWarningKey ( "BrowserWarnTest");
        }
        else
        {
          selection.setWarningKey(browserRule.getMessageKey());
        }
      }
    }
  }
}
