package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tds.exam.ApproveAccommodationsRequest;
import tds.student.services.abstractions.IAccommodationsService;
import tds.student.sql.abstractions.ExamRepository;
import tds.student.sql.data.Accommodations;
import tds.student.sql.data.OpportunityInstance;

@Service("integrationAccommodationsService")
@Scope("prototype")
public class RemoteAccommodationsService implements IAccommodationsService {
  private final IAccommodationsService legacyAccommodationsService;
  private final boolean isRemoteExamCallsEnabled;
  private final boolean isLegacyCallsEnabled;
  private final ExamRepository examRepository;

  @Autowired
  public RemoteAccommodationsService(
    @Qualifier("legacyAccommodationsService") IAccommodationsService legacyAccommodationsService,
    @Value("${tds.exam.remote.enabled}") Boolean remoteExamCallsEnabled,
    @Value("${tds.exam.legacy.enabled}") Boolean legacyCallsEnabled,
    ExamRepository examRepository) {

    if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
      throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration");
    }

    this.isRemoteExamCallsEnabled = remoteExamCallsEnabled;
    this.legacyAccommodationsService = legacyAccommodationsService;
    this.isLegacyCallsEnabled = legacyCallsEnabled;
    this.examRepository = examRepository;
  }

  @Override
  public List<Accommodations> getTestee(String testKey, boolean isGuestSession, long testeeKey) throws ReturnStatusException {
    return legacyAccommodationsService.getTestee(testKey, isGuestSession, testeeKey);
  }

  @Override
  public void approve(OpportunityInstance oppInstance, List<String> segmentsAccommodationData) throws ReturnStatusException {

    if (segmentsAccommodationData == null) {
      return;
    }
    
    if (isLegacyCallsEnabled) {
      legacyAccommodationsService.approve(oppInstance, segmentsAccommodationData);
    }

    if (!isRemoteExamCallsEnabled) {
      return;
    }

    ApproveAccommodationsRequest request = new ApproveAccommodationsRequest(oppInstance.getSessionKey(), oppInstance.getExamBrowserKey(),
      parseSegmentAccommodationStrings(segmentsAccommodationData));

    examRepository.approveAccommodations(oppInstance.getExamId(), request);
  }
  
  @Override
  public List<Accommodations> getApproved(OpportunityInstance opportunityInstance, String testKey, boolean isGuestSession) throws ReturnStatusException {
    return legacyAccommodationsService.getApproved(opportunityInstance, testKey, isGuestSession);
  }
  
  
  private Map<Integer, Set<String>> parseSegmentAccommodationStrings(List<String> segmentsAccommodationData) {
    // In format 0#TDS_Acc1,TDS_Test,ENU
    Map<Integer, Set<String>> segmentPosToAccommodationCodes = new HashMap<>();
    int segmentPosition;
    Set<String> accommCodes;
    
    for (String accomCodesDelimited : segmentsAccommodationData) {
      String[] codeStrings = accomCodesDelimited.split("#");
      segmentPosition = Integer.parseInt(codeStrings[0]);
      
      if (codeStrings.length > 1) {
        accommCodes = new HashSet(Arrays.asList(codeStrings[1].split(",")));
        segmentPosToAccommodationCodes.put(segmentPosition, accommCodes);
      }
    }
    return segmentPosToAccommodationCodes;
  }
  
}
