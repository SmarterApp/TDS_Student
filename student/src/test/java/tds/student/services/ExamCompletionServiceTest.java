package tds.student.services;

import AIR.Common.Web.TDSReplyCode;
import AIR.Common.data.ResponseData;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import tds.student.services.abstractions.IAdaptiveService;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.PageList;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.TestConfig;
import tds.student.web.TestManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamCompletionServiceTest {
    @Mock
    private IResponseService mockResposneService;

    @Mock
    private IAdaptiveService mockAdaptiveService;

    @Mock
    private IOpportunityService mockOpportunityService;

    private OpportunityInstance opportunityInstance = new OpportunityInstance(UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "exam-client-name",
        "browser-agent");

    private TestOpportunity testOpportunity = new TestOpportunity(opportunityInstance,
        "test-key",
        "test-id",
        "language",
        new TestConfig());

    @Mock
    private TestManager mockTestManager = new TestManager(testOpportunity,
        mockResposneService,
        mockAdaptiveService);

    @Mock
    private PageList mockPageList = new PageList();

    private ExamCompletionService examCompletionService;

    @Before
    public void setUp() {
        examCompletionService = new ExamCompletionServiceImpl(mockOpportunityService,
            true,
            true);
    }

    @Test
    public void shouldReviewATest() throws ReturnStatusException {
        final OpportunityStatusChange statusChange = new OpportunityStatusChange(OpportunityStatusType.parse("review"),
            true);
        final ArgumentCaptor<OpportunityInstance> opportunityInstanceArgumentCaptor =
            ArgumentCaptor.forClass(OpportunityInstance.class);
        final ArgumentCaptor<OpportunityStatusChange> opportunityStatusChangeArgumentCaptor =
            ArgumentCaptor.forClass(OpportunityStatusChange.class);

        doNothing().when(mockTestManager).LoadResponses(true);
        when(mockTestManager.GetVisiblePages()).thenReturn(mockPageList);
        when(mockTestManager.CheckIfTestComplete()).thenReturn(true);
        when(mockPageList.isAllCompleted()).thenReturn(true);
        when(mockOpportunityService.setStatus(opportunityInstance, statusChange)).thenReturn(true);

        final ResponseData<String> responseData = examCompletionService.updateStatusWithValidation(testOpportunity,
            mockTestManager,
            "review");
        verify(mockTestManager).LoadResponses(true);
        verify(mockTestManager).GetVisiblePages();
        verify(mockTestManager).CheckIfTestComplete();
        verify(mockPageList).isAllCompleted();
        verify(mockOpportunityService).setStatus(opportunityInstanceArgumentCaptor.capture(),
            opportunityStatusChangeArgumentCaptor.capture());

        assertThat(responseData.getReplyCode()).isEqualTo(TDSReplyCode.OK.getCode());
        assertThat(responseData.getData()).isNull();
        assertThat(responseData.getReplyText()).isEqualTo("OK");
        assertThat(opportunityInstanceArgumentCaptor.getValue()).isEqualTo(opportunityInstance);

        final OpportunityStatusChange capturedStatusChange = opportunityStatusChangeArgumentCaptor.getValue();
        assertThat(capturedStatusChange.getStatus()).isEqualTo(statusChange.getStatus());
        assertThat(capturedStatusChange.getReason()).isEqualTo(statusChange.getReason());
    }

    @Test
    public void shouldReturnTestLengthNotMetMessageWhenIsTestLengthMetIsFalse() throws ReturnStatusException {
        doNothing().when(mockTestManager).LoadResponses(true);
        when(mockTestManager.GetVisiblePages()).thenReturn(mockPageList);
        when(mockTestManager.CheckIfTestComplete()).thenReturn(false);
        when(mockPageList.isAllCompleted()).thenReturn(true);

        final ResponseData<String> responseData = examCompletionService.updateStatusWithValidation(testOpportunity,
            mockTestManager,
            "review");
        verify(mockTestManager).LoadResponses(true);
        verify(mockTestManager).CheckIfTestComplete();
        verifyZeroInteractions(mockOpportunityService);

        assertThat(responseData.getReplyCode()).isEqualTo(TDSReplyCode.Error.getCode());
        assertThat(responseData.getData()).isNull();
        assertThat(responseData.getReplyText()).isEqualTo("Review Test: Cannot end test because test length is not met.");
    }

    @Test
    public void shouldReturnGroupsNotAnsweredMessageWhenIsAllCompletedIsFalse() throws ReturnStatusException {
        doNothing().when(mockTestManager).LoadResponses(true);
        when(mockTestManager.GetVisiblePages()).thenReturn(mockPageList);
        when(mockTestManager.CheckIfTestComplete()).thenReturn(true);
        when(mockPageList.isAllCompleted()).thenReturn(false);

        final ResponseData<String> responseData = examCompletionService.updateStatusWithValidation(testOpportunity,
            mockTestManager,
            "review");
        verify(mockTestManager).LoadResponses(true);
        verify(mockTestManager).CheckIfTestComplete();
        verify(mockTestManager).GetVisiblePages();
        verify(mockPageList).isAllCompleted();
        verifyZeroInteractions(mockOpportunityService);

        assertThat(responseData.getReplyCode()).isEqualTo(TDSReplyCode.Error.getCode());
        assertThat(responseData.getData()).isNull();
        assertThat(responseData.getReplyText()).isEqualTo("Review Test: Cannot end test because all the groups have not been answered.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenStatusCodeIsNotReviewOrCompleted() throws ReturnStatusException {
        examCompletionService.updateStatusWithValidation(testOpportunity, mockTestManager, "pending");
    }
}

