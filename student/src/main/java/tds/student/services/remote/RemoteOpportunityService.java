package tds.student.services.remote;

import TDS.Shared.Browser.BrowserInfo;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
public class RemoteOpportunityService implements IOpportunityService {
  private final RestTemplate restTemplate;

  private final String examUrl;

  @Autowired
  public RemoteOpportunityService(
    @Qualifier("integrationRestTemplate") RestTemplate restTemplate,
    @Value("${tds.exam.remote.url}") String examUrl) {
    this.restTemplate = restTemplate;
    this.examUrl = examUrl;
  }

  @Override
  public List<TestSelection> getEligibleTests(Testee testee, TestSession session, String grade, BrowserInfo browserInfo) throws ReturnStatusException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public OpportunityInfo openTest(Testee testee, TestSession session, String testKey) throws ReturnStatusException {
    OpenExamRequest openExamRequest = new OpenExamRequest.Builder()
      .withAssessmentKey(testKey)
      .withSessionId(session.getKey())
      .withBrowserId(UUID.randomUUID())
      .withStudentId(testee.getKey())
      .build();

    HttpEntity<OpenExamRequest> requestHttpEntity = new HttpEntity<>(openExamRequest);

    ResponseEntity<Response<Exam>> res = restTemplate.exchange(
      examUrl,
      HttpMethod.POST,
      requestHttpEntity,
      new ParameterizedTypeReference<Response<Exam>>() {
      });

    Response<Exam> response = res.getBody();
    if (response.hasErrors() || !response.getData().isPresent()) {
      String errorMessage = "Failed to open exam";
      if(response.hasErrors()) {
        errorMessage = response.getErrors()[0].getMessage();
      }
      throw new ReturnStatusException(errorMessage);
    }

    Exam exam = response.getData().get();

    OpportunityInfo info = new OpportunityInfo();
    info.setBrowserKey(exam.getBrowserId());
    info.setOppKey(exam.getId());
    info.setStatus(OpportunityStatusExtensions.parseExamStatus(exam.getStatus().getCode()));

    return info;
  }

  @Override
  public OpportunityStatus getStatus(OpportunityInstance oppInstance) throws ReturnStatusException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean setStatus(OpportunityInstance oppInstance, OpportunityStatusChange statusChange) throws ReturnStatusException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public ApprovalInfo checkTestApproval(OpportunityInstance oppInstance) throws ReturnStatusException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public ApprovalInfo checkSegmentApproval(OpportunityInstance oppInstance) throws ReturnStatusException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void denyApproval(OpportunityInstance oppInstance) throws ReturnStatusException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public TestConfig startTest(OpportunityInstance oppInstance, String testKey, List<String> formKeys) throws ReturnStatusException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public OpportunitySegment.OpportunitySegments getSegments(OpportunityInstance oppInstance, boolean validate) throws ReturnStatusException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void waitForSegment(OpportunityInstance oppInstance, int segmentPosition, TestSegment.TestSegmentApproval segmentApproval) throws ReturnStatusException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void exitSegment(OpportunityInstance oppInstance, int segmentPosition) throws ReturnStatusException {
    throw new UnsupportedOperationException("Not implemented");
  }
}
