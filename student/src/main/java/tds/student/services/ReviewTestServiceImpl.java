package tds.student.services;

import AIR.Common.TDSLogger.ITDSLogger;
import AIR.Common.Web.Session.HttpContext;
import AIR.Common.Web.TDSReplyCode;
import AIR.Common.data.ResponseData;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import tds.common.ValidationError;
import tds.exam.ExamStatusCode;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.data.PageList;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.repository.remote.ExamRepository;
import tds.student.web.TestManager;

@Service
@Scope("prototype")
public class ReviewTestServiceImpl implements ReviewTestService {
    private final IOpportunityService opportunityService;
    private final ExamRepository examRepository;
    private final boolean isLegacyCallsEnabled;
    private final boolean isRemoteCallsEnabled;

    @Autowired
    public ReviewTestServiceImpl(@Qualifier("integrationOpportunityService") final IOpportunityService opportunityService,
                                 final ExamRepository examRepository,
                                 @Value("${tds.exam.legacy.enabled}") final boolean legacyCallsEnabled,
                                 @Value("${tds.exam.remote.enabled}") final boolean remoteExamCallsEnabled) {
        if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
            throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration for 'tds.exam.remote.enabled' and 'tds.exam.legacy.enabled' settings");
        }

        this.opportunityService = opportunityService;
        this.examRepository = examRepository;
        isLegacyCallsEnabled = legacyCallsEnabled;
        isRemoteCallsEnabled = remoteExamCallsEnabled;
    }

    @Override
    public ResponseData<String> reviewTest(final TestOpportunity testOpportunity, final TestManager testManager)
        throws ReturnStatusException {
        ResponseData<String> responseData = new ResponseData<>(TDSReplyCode.OK.getCode(), "OK", null);

        if (isLegacyCallsEnabled) {
            responseData = legacyReviewTest(testOpportunity, testManager);
        }

        if (!isRemoteCallsEnabled) {
            return responseData;
        }

        final Optional<ValidationError> maybeValidationError =
            examRepository.updateStatus(testOpportunity.getOppInstance().getExamId(),
                ExamStatusCode.STATUS_REVIEW,
                null);
        if (maybeValidationError.isPresent()) {
            return new ResponseData<>(TDSReplyCode.Error.getCode(),
                maybeValidationError.get().getMessage(),
                null);
        }

        return responseData;
    }

    /**
     * Executes the legacy implementation of review exam logic.
     *
     * @param testOpportunity The {@link tds.student.services.data.TestOpportunity}
     *                        {@link tds.student.services.data.TestOpportunity}
     * @param testManager     The {@link tds.student.web.TestManager} that is handling the
     *                        {@link tds.student.services.data.TestOpportunity}
     * @return A {@link AIR.Common.data.ResponseData} indicating success or failure
     * @throws ReturnStatusException In the event of a failure from one of the {@link tds.student.web.TestManager}
     *                               methods
     */
    private ResponseData<String> legacyReviewTest(final TestOpportunity testOpportunity, final TestManager testManager)
        throws ReturnStatusException {
        // get responses
        testManager.LoadResponses(true);

        // check if test length is met
        testManager.CheckIfTestComplete();
        if (!testManager.IsTestLengthMet()) {
            return new ResponseData<>(TDSReplyCode.Error.getCode(),
                "Review Test: Cannot end test because test length is not met.",
                null);
        }

        // check if all visible pages are completed
        PageList pageList = testManager.GetVisiblePages();
        if (!pageList.isAllCompleted()) {
            return new ResponseData<>(TDSReplyCode.Error.getCode(),
                "Review Test: Cannot end test because all the groups have not been answered.",
                null);
        }

        // put test in review mode
        OpportunityStatusChange statusChange =
            new OpportunityStatusChange(OpportunityStatusType.Review, true);
        opportunityService.setStatus(testOpportunity.getOppInstance(), statusChange);

        return new ResponseData<>(TDSReplyCode.OK.getCode(), "OK", null);
    }
}
