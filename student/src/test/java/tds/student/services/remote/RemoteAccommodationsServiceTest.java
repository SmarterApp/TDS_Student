package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tds.accommodation.Accommodation;
import tds.accommodation.AccommodationDependency;
import tds.assessment.Assessment;
import tds.assessment.Segment;
import tds.common.Algorithm;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.ExamAccommodation;
import tds.student.services.abstractions.IAccommodationsService;
import tds.student.sql.repository.AssessmentRepository;
import tds.student.sql.repository.ExamRepository;
import tds.student.sql.data.AccommodationType;
import tds.student.sql.data.AccommodationValue;
import tds.student.sql.data.Accommodations;
import tds.student.sql.data.OpportunityInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteAccommodationsServiceTest {
    @Mock
    private IAccommodationsService legacyAccommodationsService;
    
    private IAccommodationsService service;
    
    @Mock
    private ExamRepository mockExamRepository;
    
    @Mock
    private AssessmentRepository mockAssessmentRepository;
    
    @Before
    public void setup() {
        service = new RemoteAccommodationsService(legacyAccommodationsService, true, true, mockExamRepository,
            mockAssessmentRepository);
    }
    
    @Test
    public void shouldCallApproveAccommodations() throws ReturnStatusException {
        OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<String> segmentData = Arrays.asList("0#AccCode1,AccCode2", "1#AccCode3", "2#AccCode4");
        service.approve(oppInstance, segmentData);
        verify(legacyAccommodationsService).approve(oppInstance, segmentData);
        verify(mockExamRepository).approveAccommodations(eq(oppInstance.getExamId()), (ApproveAccommodationsRequest) any());
    }
    
    @Test
    public void shouldCallApproveAccommodationsWithSpacesBetweenCommas() throws ReturnStatusException {
        OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<String> segmentData = Arrays.asList("0#AccCode1, AccCode2", "1#AccCode3", "2#AccCode4");
        service.approve(oppInstance, segmentData);
        verify(legacyAccommodationsService).approve(oppInstance, segmentData);
        verify(mockExamRepository).approveAccommodations(eq(oppInstance.getExamId()), (ApproveAccommodationsRequest) any());
    }
    
    @Test(expected=NumberFormatException.class)
    public void shouldFailToApproveAccommodationsForBadSegmentData2() throws ReturnStatusException {
        OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<String> segmentData = Arrays.asList("#AccCode1,AccCode2", "#AccCode3", "#AccCode4");
        service.approve(oppInstance, segmentData);
        verify(legacyAccommodationsService).approve(oppInstance, segmentData);
        verify(mockExamRepository).approveAccommodations(eq(oppInstance.getExamId()), (ApproveAccommodationsRequest) any());
    }
    
    @Test(expected=NumberFormatException.class)
    public void shouldFailToApproveAccommodationsForBadSegmentData() throws ReturnStatusException {
        OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<String> segmentData = Arrays.asList("0AccCode1,AccCode2", "AccCode3", "2#AccCode4");
        service.approve(oppInstance, segmentData);
        verify(legacyAccommodationsService).approve(oppInstance, segmentData);
        verify(mockExamRepository).approveAccommodations(eq(oppInstance.getExamId()), (ApproveAccommodationsRequest) any());
    }
    
    @Test
    public void shouldDoNothingIfNullSegmentData() throws ReturnStatusException {
        OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        service.approve(oppInstance, null);
        verify(legacyAccommodationsService, never()).approve(oppInstance, null);
        verify(mockExamRepository, never()).approveAccommodations(eq(oppInstance.getExamId()), (ApproveAccommodationsRequest) any());
    }
    
    @Test
    public void shouldApproveNothingForEmptyAssessmentAccommodations() throws ReturnStatusException {
        final String assessmentKey = "assessment-key";
        final String assessmentId = "assessment-id";
        final String clientName = "SBAC_PT";
        final UUID examId = UUID.randomUUID();
        OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          examId, UUID.randomUUID(), clientName);
    
        AccommodationDependency dependency = new AccommodationDependency.Builder(assessmentId)
          .withIfType("Language")
          .withIfValue("ELA")
          .withThenType("Masking")
          .withThenValue("Off")
          .withIsDefault(true)
          .build();
    
        Segment seg1 = new Segment("segmentKey1", Algorithm.ADAPTIVE_2);
        seg1.setLabel("ELA Segment 1");
        seg1.setSegmentId("segment-id-1");
        seg1.setPosition(1);
        Segment seg2 = new Segment("segmentKey2", Algorithm.ADAPTIVE_2);
        seg2.setLabel("ELA Segment 2");
        seg2.setSegmentId("segment-id-2");
        seg2.setPosition(2);
    
        Assessment assessment = new Assessment();
        assessment.setKey(assessmentKey);
        assessment.setAssessmentId(assessmentId);
        assessment.setLabel("ELA Test");
        assessment.setAccommodationDependencies(Arrays.asList(dependency));
        assessment.setSegments(Arrays.asList(seg1, seg2));
        
        ExamAccommodation assessmentExamAcc1 = new ExamAccommodation.Builder(UUID.randomUUID())
          .withCode("AccType1")
          .withType("AccType1")
          .build();
        ExamAccommodation segment1ExamAcc = new ExamAccommodation.Builder(UUID.randomUUID())
          .withCode("AccType2")
          .withType("AccType2")
          .build();
        ExamAccommodation segment2ExamAcc = new ExamAccommodation.Builder(UUID.randomUUID())
          .withCode("AccType3")
          .withType("AccType3")
          .build();
    
        when(legacyAccommodationsService.getApproved(oppInstance, assessmentKey, true)).thenReturn(new ArrayList<Accommodations>());
        when(mockAssessmentRepository.findAccommodations(clientName, assessmentKey)).thenReturn(new ArrayList<Accommodation>());
        when(mockAssessmentRepository.findAssessment(clientName, assessmentKey)).thenReturn(assessment);
        when(mockExamRepository.findApprovedAccommodations(examId)).thenReturn(Arrays.asList(assessmentExamAcc1, segment1ExamAcc, segment2ExamAcc));
    
        List<Accommodations> retAccommodations = service.getApproved(oppInstance, assessmentKey, true);
    
        verify(legacyAccommodationsService).getApproved(oppInstance, assessmentKey, true);
        verify(mockAssessmentRepository).findAccommodations(clientName, assessmentKey);
        verify(mockAssessmentRepository).findAssessment(clientName, assessmentKey);
        verify(mockExamRepository).findApprovedAccommodations(examId);

        for (Accommodations accommodations : retAccommodations) {
            assertThat(accommodations.getTypes()).isEmpty();
        }
    }
    
    @Test
    public void shouldApproveNothingForEmptyExamAccommodations() throws ReturnStatusException {
        final String assessmentKey = "assessment-key";
        final String assessmentId = "assessment-id";
        final String clientName = "SBAC_PT";
        final UUID examId = UUID.randomUUID();
        OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          examId, UUID.randomUUID(), clientName);
    
        AccommodationDependency dependency = new AccommodationDependency.Builder(assessmentId)
          .withIfType("Language")
          .withIfValue("ELA")
          .withThenType("Masking")
          .withThenValue("Off")
          .withIsDefault(true)
          .build();
    
        Segment seg1 = new Segment("segmentKey1", Algorithm.ADAPTIVE_2);
        seg1.setLabel("ELA Segment 1");
        seg1.setSegmentId("segment-id-1");
        seg1.setPosition(1);
        Segment seg2 = new Segment("segmentKey2", Algorithm.ADAPTIVE_2);
        seg2.setLabel("ELA Segment 2");
        seg2.setSegmentId("segment-id-2");
        seg2.setPosition(2);
    
        Assessment assessment = new Assessment();
        assessment.setKey(assessmentKey);
        assessment.setAssessmentId(assessmentId);
        assessment.setLabel("ELA Test");
        assessment.setAccommodationDependencies(Arrays.asList(dependency));
        assessment.setSegments(Arrays.asList(seg1, seg2));
    
        Accommodation assessmentAcc1 = new Accommodation.Builder()
          .withContext(assessmentId)
          .withAccommodationType("AccType1")
          .withAccommodationCode("AccCode1")
          .withSegmentPosition(0)
          .withAllowChange(true)
          .withAllowCombine(false)
          .withDependsOnToolType("Language")
          .build();
        Accommodation assessmentAcc2 = new Accommodation.Builder()
          .withContext(assessmentId)
          .withAccommodationType("AccType2")
          .withAccommodationCode("AccCode2")
          .withSegmentPosition(0)
          .withAllowChange(true)
          .withAllowCombine(false)
          .withDependsOnToolType("Language")
          .build();
        Accommodation segment1Acc = new Accommodation.Builder()
          .withContext(seg1.getSegmentId())
          .withAccommodationType("AccType3")
          .withAccommodationCode("AccCode3")
          .withSegmentPosition(1)
          .withAllowChange(true)
          .withAllowCombine(false)
          .withDependsOnToolType("Language")
          .build();
        Accommodation segment2Acc = new Accommodation.Builder()
          .withContext(seg2.getSegmentId())
          .withAccommodationType("AccType4")
          .withAccommodationCode("AccCode4")
          .withSegmentPosition(2)
          .withAllowChange(true)
          .withAllowCombine(false)
          .build();
        
        when(legacyAccommodationsService.getApproved(oppInstance, assessmentKey, true)).thenReturn(new ArrayList<Accommodations>());
        when(mockAssessmentRepository.findAccommodations(clientName, assessmentKey))
          .thenReturn(Arrays.asList(assessmentAcc1, assessmentAcc2, segment1Acc, segment2Acc));
        when(mockAssessmentRepository.findAssessment(clientName, assessmentKey)).thenReturn(assessment);
        when(mockExamRepository.findApprovedAccommodations(examId)).thenReturn(new ArrayList<ExamAccommodation>());
    
        List<Accommodations> retAccommodations = service.getApproved(oppInstance, assessmentKey, true);
    
        verify(legacyAccommodationsService).getApproved(oppInstance, assessmentKey, true);
        verify(mockAssessmentRepository).findAccommodations(clientName, assessmentKey);
        verify(mockAssessmentRepository).findAssessment(clientName, assessmentKey);
        verify(mockExamRepository).findApprovedAccommodations(examId);
        
        for (Accommodations accommodations : retAccommodations) {
            assertThat(accommodations.getTypes()).isEmpty();
        }
    }
    
    @Test
    public void shouldGetAccommodationsForGuestSessionWithSegments() throws ReturnStatusException {
        final String assessmentKey = "assessment-key";
        final String assessmentId = "assessment-id";
        final String clientName = "SBAC_PT";
        final UUID examId = UUID.randomUUID();
        OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          examId, UUID.randomUUID(), clientName);
    
        AccommodationDependency dependency = new AccommodationDependency.Builder(assessmentId)
          .withIfType("Language")
          .withIfValue("ELA")
          .withThenType("Masking")
          .withThenValue("Off")
          .withIsDefault(true)
          .build();
    
        Segment seg1 = new Segment("segmentKey1", Algorithm.ADAPTIVE_2);
        seg1.setLabel("ELA Segment 1");
        seg1.setSegmentId("segment-id-1");
        seg1.setPosition(1);
        Segment seg2 = new Segment("segmentKey2", Algorithm.ADAPTIVE_2);
        seg2.setLabel("ELA Segment 2");
        seg2.setSegmentId("segment-id-2");
        seg2.setPosition(2);
        
        Assessment assessment = new Assessment();
        assessment.setKey(assessmentKey);
        assessment.setAssessmentId(assessmentId);
        assessment.setLabel("ELA Test");
        assessment.setAccommodationDependencies(Arrays.asList(dependency));
        assessment.setSegments(Arrays.asList(seg1, seg2));
    
        Accommodation assessmentAcc1 = new Accommodation.Builder()
          .withContext(assessmentId)
          .withAccommodationType("AccType1")
          .withAccommodationCode("AccCode1")
          .withSegmentPosition(0)
          .withAllowChange(true)
          .withAllowCombine(false)
          .withDependsOnToolType("Language")
          .build();
        Accommodation assessmentAcc2 = new Accommodation.Builder()
          .withContext(assessmentId)
          .withAccommodationType("AccType2")
          .withAccommodationCode("AccCode2")
          .withSegmentPosition(0)
          .withAllowChange(true)
          .withAllowCombine(false)
          .withDependsOnToolType("Language")
          .build();
        Accommodation assessmentAcc3 = new Accommodation.Builder()
          .withContext(assessmentId)
          .withAccommodationType("AccType3")
          .withAccommodationCode("AccCode3")
          .withSegmentPosition(0)
          .withAllowChange(true)
          .withAllowCombine(false)
          .withDisableOnGuestSession(true)
          .withDependsOnToolType("Language")
          .build();
        Accommodation segment1Acc = new Accommodation.Builder()
          .withContext(seg1.getSegmentId())
          .withAccommodationType("AccType4")
          .withAccommodationCode("AccCode4")
          .withSegmentPosition(1)
          .withAllowChange(true)
          .withAllowCombine(false)
          .withDependsOnToolType("Language")
          .build();
        Accommodation segment2Acc = new Accommodation.Builder()
          .withContext(seg2.getSegmentId())
          .withAccommodationType("AccType5")
          .withAccommodationCode("AccCode5")
          .withSegmentPosition(2)
          .withAllowChange(true)
          .withAllowCombine(false)
          .build();
    
        ExamAccommodation assessmentExamAcc1 = new ExamAccommodation.Builder(UUID.randomUUID())
          .withCode(assessmentAcc1.getCode())
          .withType(assessmentAcc1.getType())
          .build();
        ExamAccommodation badAssessmentExamAcc2 = new ExamAccommodation.Builder(UUID.randomUUID())
          .withCode("DifferentCode")
          .withType(assessmentAcc2.getType())
          .build();
        ExamAccommodation disabledGuestAccom = new ExamAccommodation.Builder(UUID.randomUUID())
          .withType(assessmentAcc3.getType())
          .withCode(assessmentAcc3.getCode())
          .build();
        ExamAccommodation segment1ExamAcc = new ExamAccommodation.Builder(UUID.randomUUID())
          .withType(segment1Acc.getType())
          .withCode(segment1Acc.getCode())
          .build();
        ExamAccommodation segment2ExamAcc = new ExamAccommodation.Builder(UUID.randomUUID())
          .withType(segment2Acc.getType())
          .withCode(segment2Acc.getCode())
          .build();
        
        when(legacyAccommodationsService.getApproved(oppInstance, assessmentKey, true)).thenReturn(new ArrayList<Accommodations>());
        when(mockAssessmentRepository.findAccommodations(clientName, assessmentKey))
          .thenReturn(Arrays.asList(assessmentAcc1, assessmentAcc2, assessmentAcc3, segment1Acc, segment2Acc));
        when(mockAssessmentRepository.findAssessment(clientName, assessmentKey)).thenReturn(assessment);
        when(mockExamRepository.findApprovedAccommodations(examId))
          .thenReturn(Arrays.asList(assessmentExamAcc1, badAssessmentExamAcc2, disabledGuestAccom, segment1ExamAcc, segment2ExamAcc));
        
        List<Accommodations> retAccommodations = service.getApproved(oppInstance, assessmentKey, true);
        
        verify(legacyAccommodationsService).getApproved(oppInstance, assessmentKey, true);
        verify(mockAssessmentRepository).findAccommodations(clientName, assessmentKey);
        verify(mockAssessmentRepository).findAssessment(clientName, assessmentKey);
        verify(mockExamRepository).findApprovedAccommodations(examId);
        
        assertThat(retAccommodations).hasSize(3);
        
        Accommodations assessmentAccommodations = null;
        Accommodations segment1Accommodations = null;
        Accommodations segment2Accommodations = null;
        
        for (Accommodations accommodations : retAccommodations) {
            if (accommodations.getPosition() == 0) {
                assessmentAccommodations = accommodations;
            } else if (accommodations.getPosition() == 1) {
                segment1Accommodations = accommodations;
            } else if (accommodations.getPosition() == 2) {
                segment2Accommodations = accommodations;
            }
        }
        // Assessment Accommodations
        assertThat(assessmentAccommodations.getLabel()).isEqualTo(assessment.getLabel());
        assertThat(assessmentAccommodations.getId()).isEqualTo(assessment.getAssessmentId());
        assertThat(assessmentAccommodations.getTypes()).hasSize(1);
        
        AccommodationType assessmentAccType = assessmentAccommodations.getTypes().get(0);
        assertThat(assessmentAccType.getDependsOnToolType()).isEqualTo(assessmentAcc1.getDependsOnToolType());
        assertThat(assessmentAccType.getName()).isEqualTo(assessmentAcc1.getType());
        String assessmentAccCode = ((AccommodationValue) assessmentAccType.getValues().toArray()[0]).getCode();
        assertThat(assessmentAccCode).isEqualTo(assessmentAcc1.getCode());
        
        // Segment 1 accommodations
        assertThat(segment1Accommodations.getLabel()).isEqualTo(seg1.getLabel());
        assertThat(segment1Accommodations.getId()).isEqualTo(seg1.getSegmentId());
        assertThat(segment1Accommodations.getTypes()).hasSize(1);
    
        AccommodationType segment1AccType = segment1Accommodations.getTypes().get(0);
        assertThat(segment1AccType.getDependsOnToolType()).isEqualTo(segment1Acc.getDependsOnToolType());
        assertThat(segment1AccType.getName()).isEqualTo(segment1Acc.getType());
        String seg1AccCode = ((AccommodationValue) segment1AccType.getValues().toArray()[0]).getCode();
        assertThat(seg1AccCode).isEqualTo(segment1Acc.getCode());
        
        // Segment 2 accommodations
        assertThat(segment2Accommodations.getLabel()).isEqualTo(seg2.getLabel());
        assertThat(segment2Accommodations.getId()).isEqualTo(seg2.getSegmentId());
        assertThat(segment2Accommodations.getTypes()).hasSize(1);
    
        AccommodationType segment2AccType = segment2Accommodations.getTypes().get(0);
        assertThat(segment2AccType.getDependsOnToolType()).isEqualTo(segment2Acc.getDependsOnToolType());
        assertThat(segment2AccType.getName()).isEqualTo(segment2Acc.getType());
        String seg2AccCode = ((AccommodationValue) segment2AccType.getValues().toArray()[0]).getCode();
        assertThat(seg2AccCode).isEqualTo(segment2Acc.getCode());
    }
}
