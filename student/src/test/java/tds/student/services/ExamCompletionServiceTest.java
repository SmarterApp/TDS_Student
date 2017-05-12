package tds.student.services;

import AIR.Common.Web.TDSReplyCode;
import AIR.Common.data.ResponseData;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import tds.common.ValidationError;
import tds.student.services.abstractions.IAdaptiveService;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.PageList;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.TestConfig;
import tds.student.sql.repository.remote.ExamRepository;
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

    @Mock
    private ExamRepository remoteExamRepository;

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
            remoteExamRepository,
            true,
            true);
    }

    @Test
    public void shouldReviewATestWhenLegacyCallsAndRemoteCallsAreEnabled() throws ReturnStatusException {
        doNothing().when(mockTestManager).LoadResponses(true);
        when(mockTestManager.GetVisiblePages()).thenReturn(mockPageList);
        when(mockTestManager.CheckIfTestComplete()).thenReturn(true);
        when(mockTestManager.IsTestLengthMet()).thenReturn(true);
        when(mockPageList.isAllCompleted()).thenReturn(true);
        when(remoteExamRepository.updateStatus(opportunityInstance.getExamId(), "review", null))
            .thenReturn(Optional.<ValidationError>absent());

        final ResponseData<String> responseData = examCompletionService.updateStatusWithValidation(testOpportunity, mockTestManager, "review");
        verify(mockTestManager).LoadResponses(true);
        verify(mockTestManager).GetVisiblePages();
        verify(mockTestManager).CheckIfTestComplete();
        verify(mockTestManager).IsTestLengthMet();
        verify(mockPageList).isAllCompleted();
        verify(remoteExamRepository).updateStatus(opportunityInstance.getExamId(), "review", null);

        assertThat(responseData.getReplyCode()).isEqualTo(TDSReplyCode.OK.getCode());
        assertThat(responseData.getData()).isNull();
        assertThat(responseData.getReplyText()).isEqualTo("OK");
    }

    @Test
    public void shouldReviewATestWhenLegacyCallsAreEnabledButRemoteCallsAreDisabled() throws ReturnStatusException {
        examCompletionService = new ExamCompletionServiceImpl(mockOpportunityService,
            remoteExamRepository,
            true,
            false);

        doNothing().when(mockTestManager).LoadResponses(true);
        when(mockTestManager.GetVisiblePages()).thenReturn(mockPageList);
        when(mockTestManager.CheckIfTestComplete()).thenReturn(true);
        when(mockTestManager.IsTestLengthMet()).thenReturn(true);
        when(mockPageList.isAllCompleted()).thenReturn(true);
        when(remoteExamRepository.updateStatus(opportunityInstance.getExamId(), "review", null))
            .thenReturn(Optional.<ValidationError>absent());

        final ResponseData<String> responseData = examCompletionService.updateStatusWithValidation(testOpportunity, mockTestManager, "review");
        verify(mockTestManager).LoadResponses(true);
        verify(mockTestManager).GetVisiblePages();
        verify(mockTestManager).CheckIfTestComplete();
        verify(mockTestManager).IsTestLengthMet();
        verify(mockPageList).isAllCompleted();
        verifyZeroInteractions(remoteExamRepository);

        assertThat(responseData.getReplyCode()).isEqualTo(TDSReplyCode.OK.getCode());
        assertThat(responseData.getData()).isNull();
        assertThat(responseData.getReplyText()).isEqualTo("OK");
    }

    @Test
    public void ShouldReviewATestWhenLegacyCallsAreDisabledButRemoteCallsAreEnabled() throws ReturnStatusException {
        examCompletionService = new ExamCompletionServiceImpl(mockOpportunityService,
            remoteExamRepository,
            false,
            true);

        doNothing().when(mockTestManager).LoadResponses(true);
        when(mockTestManager.GetVisiblePages()).thenReturn(mockPageList);
        when(mockTestManager.CheckIfTestComplete()).thenReturn(true);
        when(mockTestManager.IsTestLengthMet()).thenReturn(true);
        when(mockPageList.isAllCompleted()).thenReturn(true);
        when(remoteExamRepository.updateStatus(opportunityInstance.getExamId(), "review", null))
            .thenReturn(Optional.<ValidationError>absent());

        final ResponseData<String> responseData = examCompletionService.updateStatusWithValidation(testOpportunity, mockTestManager, "review");

        // Since the ExamCompletionServiceImpl#legacyReviewTest method is private, we can verify that the legacy entities
        // are not being interacted with.
        verify(remoteExamRepository).updateStatus(opportunityInstance.getExamId(), "review", null);
        verifyZeroInteractions(mockTestManager);
        verifyZeroInteractions(mockPageList);

        assertThat(responseData.getReplyCode()).isEqualTo(TDSReplyCode.OK.getCode());
        assertThat(responseData.getData()).isNull();
        assertThat(responseData.getReplyText()).isEqualTo("OK");
    }

    @Test
    public void shouldReturnTestLengthNotMetMessageWhenLegacyCallsAreEnabledAndIsTestLengthMetIsFalse() throws ReturnStatusException {
        doNothing().when(mockTestManager).LoadResponses(true);
        when(mockTestManager.GetVisiblePages()).thenReturn(mockPageList);
        when(mockTestManager.CheckIfTestComplete()).thenReturn(true);
        when(mockTestManager.IsTestLengthMet()).thenReturn(false);
        when(mockPageList.isAllCompleted()).thenReturn(true);
        when(remoteExamRepository.updateStatus(opportunityInstance.getExamId(), "review", null))
            .thenReturn(Optional.<ValidationError>absent());

        final ResponseData<String> responseData = examCompletionService.updateStatusWithValidation(testOpportunity, mockTestManager, "review");

        assertThat(responseData.getReplyCode()).isEqualTo(TDSReplyCode.Error.getCode());
        assertThat(responseData.getData()).isNull();
        assertThat(responseData.getReplyText()).isEqualTo("Review Test: Cannot end test because test length is not met.");
    }

    @Test
    public void shouldReturnGroupsNotAnsweredMessageWhenLegacyCallsAreEnabledAndIsAllCompletedIsFalse() throws ReturnStatusException {
        doNothing().when(mockTestManager).LoadResponses(true);
        when(mockTestManager.GetVisiblePages()).thenReturn(mockPageList);
        when(mockTestManager.CheckIfTestComplete()).thenReturn(true);
        when(mockTestManager.IsTestLengthMet()).thenReturn(true);
        when(mockPageList.isAllCompleted()).thenReturn(false);
        when(remoteExamRepository.updateStatus(opportunityInstance.getExamId(), "review", null))
            .thenReturn(Optional.<ValidationError>absent());

        final ResponseData<String> responseData = examCompletionService.updateStatusWithValidation(testOpportunity, mockTestManager, "review");

        assertThat(responseData.getReplyCode()).isEqualTo(TDSReplyCode.Error.getCode());
        assertThat(responseData.getData()).isNull();
        assertThat(responseData.getReplyText()).isEqualTo("Review Test: Cannot end test because all the groups have not been answered.");
    }
}

