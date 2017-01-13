package tds.student.services.remote;

import TDS.Shared.Browser.BrowserInfo;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.exam.Exam;
import tds.exam.OpenExamRequest;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.data.ApprovalInfo;
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
  private final RestTemplate restTemplate;
  private final IOpportunityService legacyOpportunityService;
  private final String examUrl;
  private final boolean isRemoteExamCallsEnabled;

  @Autowired
  public RemoteOpportunityService(
    @Qualifier("integrationRestTemplate") RestTemplate restTemplate,
    @Qualifier("legacyOpportunityService") IOpportunityService legacyOpportunityService,
    @Value("${tds.exam.remote.url}") String examUrl,
    @Value("${tds.exam.remote.enabled}") Boolean remoteExamCallsEnabled) {
    this.restTemplate = restTemplate;
    this.examUrl = examUrl;
    this.isRemoteExamCallsEnabled = remoteExamCallsEnabled;
    this.legacyOpportunityService = legacyOpportunityService;
  }

  @Override
  public List<TestSelection> getEligibleTests(Testee testee, TestSession session, String grade, BrowserInfo browserInfo) throws ReturnStatusException {
    return legacyOpportunityService.getEligibleTests(testee, session, grade, browserInfo);
  }

  @Override
  public OpportunityInfo openTest(Testee testee, TestSession session, String testKey) throws ReturnStatusException {
    OpportunityInfo legacyOpportunityInfo = legacyOpportunityService.openTest(testee, session, testKey);

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

    HttpEntity<OpenExamRequest> requestHttpEntity = new HttpEntity<>(openExamRequest);
    ResponseEntity<Response<Exam>> responseEntity;

    try {
      responseEntity = restTemplate.exchange(
        examUrl,
        HttpMethod.POST,
        requestHttpEntity,
        new ParameterizedTypeReference<Response<Exam>>() {
        });
    } catch (RestClientException rce) {
      throw new ReturnStatusException(rce);
    }

    Response<Exam> response = responseEntity.getBody();

    if (!response.hasErrors() && !response.getData().isPresent()) {
      throw new ReturnStatusException("Invalid response from the exam service");
    } else if (response.hasErrors()) {
      String errorMessage = "Failed to open exam";
      if(response.hasErrors()) {
        errorMessage = response.getErrors()[0].getMessage();
      }
      throw new ReturnStatusException(errorMessage);
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
  public OpportunityStatus getStatus(OpportunityInstance oppInstance) throws ReturnStatusException {
    return legacyOpportunityService.getStatus(oppInstance);
  }

  @Override
  public boolean setStatus(OpportunityInstance oppInstance, OpportunityStatusChange statusChange) throws ReturnStatusException {
    return legacyOpportunityService.setStatus(oppInstance, statusChange);
  }

  @Override
  public ApprovalInfo checkTestApproval(OpportunityInstance oppInstance) throws ReturnStatusException {
    return legacyOpportunityService.checkTestApproval(oppInstance);
  }

  @Override
  public ApprovalInfo checkSegmentApproval(OpportunityInstance oppInstance) throws ReturnStatusException {
    return legacyOpportunityService.checkSegmentApproval(oppInstance);
  }

  @Override
  public void denyApproval(OpportunityInstance oppInstance) throws ReturnStatusException {
    legacyOpportunityService.denyApproval(oppInstance);
  }

  @Override
  public TestConfig startTest(OpportunityInstance oppInstance, String testKey, List<String> formKeys) throws ReturnStatusException {
    return legacyOpportunityService.startTest(oppInstance, testKey, formKeys);
  }

  @Override
  public OpportunitySegment.OpportunitySegments getSegments(OpportunityInstance oppInstance, boolean validate) throws ReturnStatusException {
    return legacyOpportunityService.getSegments(oppInstance, validate);
  }

  @Override
  public void waitForSegment(OpportunityInstance oppInstance, int segmentPosition, TestSegment.TestSegmentApproval segmentApproval) throws ReturnStatusException {
    legacyOpportunityService.waitForSegment(oppInstance, segmentPosition, segmentApproval);
  }

  @Override
  public void exitSegment(OpportunityInstance oppInstance, int segmentPosition) throws ReturnStatusException {
    legacyOpportunityService.exitSegment(oppInstance, segmentPosition);
  }
}
