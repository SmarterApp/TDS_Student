package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tds.accommodation.Accommodation;
import tds.accommodation.AccommodationDependency;
import tds.assessment.Assessment;
import tds.assessment.Segment;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.ExamAccommodation;
import tds.student.services.abstractions.IAccommodationsService;
import tds.student.sql.repository.AssessmentRepository;
import tds.student.sql.repository.ExamRepository;
import tds.student.sql.data.Accommodations;
import tds.student.sql.data.OpportunityInstance;

@Service("integrationAccommodationsService")
@Scope("prototype")
public class RemoteAccommodationsService implements IAccommodationsService {
  private final IAccommodationsService legacyAccommodationsService;
  private final boolean isRemoteExamCallsEnabled;
  private final boolean isLegacyCallsEnabled;
  private final ExamRepository examRepository;
  private final AssessmentRepository assessmentRepository;
  private static final int ASSESSMENT_POSITION = 0;
  
  @Autowired
  public RemoteAccommodationsService(
    @Qualifier("legacyAccommodationsService") IAccommodationsService legacyAccommodationsService,
    @Value("${tds.exam.remote.enabled}") Boolean remoteExamCallsEnabled,
    @Value("${tds.exam.legacy.enabled}") Boolean legacyCallsEnabled,
    ExamRepository examRepository,
    AssessmentRepository assessmentRepository) {
    
    if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
      throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration");
    }
    
    this.isRemoteExamCallsEnabled = remoteExamCallsEnabled;
    this.legacyAccommodationsService = legacyAccommodationsService;
    this.isLegacyCallsEnabled = legacyCallsEnabled;
    this.examRepository = examRepository;
    this.assessmentRepository = assessmentRepository;
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
  public List<Accommodations> getApproved(final OpportunityInstance opportunityInstance, final String testKey,
                                          final boolean isGuestSession) throws ReturnStatusException {
    List<Accommodations> accommodations = null;
    
    if (isLegacyCallsEnabled) {
      accommodations = legacyAccommodationsService.getApproved(opportunityInstance, testKey, isGuestSession);
    }
    
    if (!isRemoteExamCallsEnabled) {
      return accommodations;
    }
    /* AccommodationService [192] */
    Assessment assessment = assessmentRepository.findAssessment(opportunityInstance.getExamClientName(), testKey);
    // Contains all approved accommodations for this exam, for the entire assessment and individual segments
    /* AccommodationService [194] */
    List<ExamAccommodation> examAccommodationResponse = examRepository.findApprovedAccommodations(opportunityInstance.getExamId());
    /* [396] This should contain accommodations for the entire assessment and its individual segments. */
    // Contains all accommodation data
    List<Accommodation> assessmentAccommodationsResponse = assessmentRepository.findAccommodations(opportunityInstance.getExamClientName(), testKey);
    
    return mapExamAccommodationsToLegacyAccommodations(assessment, assessmentAccommodationsResponse, examAccommodationResponse, isGuestSession);
  }
  
  /* This method is a port of AccommodationService.getTestSegments() between [398 - 426] with some logic located in getApproved()
  *  The "Accommodations" class is what the legacy code will use to create the TDS-Student-Accs cookie */
  private List<Accommodations> mapExamAccommodationsToLegacyAccommodations(final Assessment assessment, final List<Accommodation> assessmentAccommodations,
                                                     final List<ExamAccommodation> approvedExamAccommodations, final boolean isGuestSession) {
    // accCode -> ExamAccommodation
    Map<String, ExamAccommodation> approvedExamAccommodationMap = new HashMap<>();
    // segmentPosition -> legacy Accommodations object (per segment + assessment)
    Map<Integer, Accommodations> segmentPositionToAccommodations = new HashMap<>();
    Accommodations retAccommodations;
    ExamAccommodation approvedExamAccommodation;
    int segmentPosition;
    
    // Map the accommodation keys (code) to their exam accommodations for quick lookup
    for (ExamAccommodation examAccommodation : approvedExamAccommodations) {
      approvedExamAccommodationMap.put(examAccommodation.getCode(), examAccommodation);
    }
    
    /* We need to filter only approve ExamAccommodations - although the Assessment accommodations contain
       the majority of the accommodation metadata  */
    for (Accommodation accommodation : assessmentAccommodations) {
      // Filter only approved accommodations
      approvedExamAccommodation = approvedExamAccommodationMap.get(accommodation.getCode());
    
      if (approvedExamAccommodation != null && approvedExamAccommodation.getType().equalsIgnoreCase(accommodation.getType())) {
        segmentPosition = accommodation.getSegmentPosition();
        retAccommodations = segmentPositionToAccommodations.get(segmentPosition);
      
        // If there are no Accommodations created for this segment position, we need to create one.
        if (retAccommodations == null) {
          // Accommodation context is either the segmentId or assessmentId, depending on whether the accommodation is
          // segment specific or global to the assessment
          retAccommodations = new Accommodations(segmentPosition, accommodation.getContext(), getLabelForAssessmentOrSegment(segmentPosition, assessment));
          segmentPositionToAccommodations.put(segmentPosition, retAccommodations);
        }
      
        /* AccommodationService [399-400] - If this isn't a guest session, then no need to check isDisabledOnGuest flag */
        if (!isGuestSession || !accommodation.isDisableOnGuestSession()) {
          // Create accommodations type and value - these will be used by the UI between checkApproval and startTest
          retAccommodations.create(accommodation.getType(), accommodation.getCode(), accommodation.getValue(), accommodation.isVisible(),
            accommodation.isSelectable(), accommodation.isAllowChange(), accommodation.isStudentControl(), accommodation.getDependsOnToolType(),
            accommodation.isDisableOnGuestSession(), accommodation.isDefaultAccommodation(), accommodation.isAllowCombine(), true);
        }
      }
    }
  
    // Populate the accommodation dependencies for the *assessment* accommodation
    if (!segmentPositionToAccommodations.isEmpty()) {
      Accommodations assessmentAccommodation = segmentPositionToAccommodations.get(ASSESSMENT_POSITION);
    
      for (AccommodationDependency assessmentDependency : assessment.getAccommodationDependencies()) {
        assessmentAccommodation.AddDependency(assessmentDependency.getIfType(), assessmentDependency.getIfValue(),
          assessmentDependency.getThenType(), assessmentDependency.getThenValue(), assessmentDependency.isDefault());
      }
    }
    
    return new ArrayList<>(segmentPositionToAccommodations.values());
  }

  private String getLabelForAssessmentOrSegment(final int position, final Assessment assessment) {
    // Position of zero is an assessment
    if (position == ASSESSMENT_POSITION) {
      return assessment.getLabel();
    }
    
    String segmentLabel = null;
    for (Segment segment : assessment.getSegments()) {
      if (segment.getPosition() == position) {
        segmentLabel = segment.getLabel();
      }
    }
    
    return segmentLabel;
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
