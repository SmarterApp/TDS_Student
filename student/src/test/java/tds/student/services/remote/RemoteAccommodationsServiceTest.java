package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tds.exam.ApproveAccommodationsRequest;
import tds.student.services.abstractions.IAccommodationsService;
import tds.student.sql.abstractions.ExamRepository;
import tds.student.sql.data.OpportunityInstance;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RemoteAccommodationsServiceTest {
    @Mock
    private IAccommodationsService legacyAccommodationsService;
    
    private IAccommodationsService service;
    
    @Mock
    private ExamRepository mockExamRepository;
    
    @Before
    public void setup() {
        service = new RemoteAccommodationsService(legacyAccommodationsService, true, true, mockExamRepository);
    }
    
    @Test
    public void shouldCallApproveAccommodations() throws ReturnStatusException {
        OpportunityInstance oppInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<String> segmentData = Arrays.asList("0#AccCode1,AccCode2", "1#AccCode3", "2#AccCode4");
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
        verify(legacyAccommodationsService).approve(oppInstance, null);
        verify(mockExamRepository, never()).approveAccommodations(eq(oppInstance.getExamId()), (ApproveAccommodationsRequest) any());
    }
}
