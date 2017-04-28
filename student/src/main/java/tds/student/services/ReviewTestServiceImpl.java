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
    private final ITDSLogger tdsLogger;
    private final boolean isLegacyCallsEnabled;
    private final boolean isRemoteCallsEnabled;

    @Autowired
    public ReviewTestServiceImpl(@Qualifier("integrationOpportunityService") final IOpportunityService opportunityService,
                                 final ExamRepository examRepository,
                                 final ITDSLogger tdsLogger,
                                 @Value("${tds.exam.legacy.enabled}") final boolean legacyCallsEnabled,
                                 @Value("${tds.exam.remote.enabled}") final boolean remoteExamCallsEnabled) {
        if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
            throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration for 'tds.exam.remote.enabled' and 'tds.exam.legacy.enabled' settings");
        }

        this.opportunityService = opportunityService;
        this.examRepository = examRepository;
        this.tdsLogger = tdsLogger;
        isLegacyCallsEnabled = legacyCallsEnabled;
        isRemoteCallsEnabled = remoteExamCallsEnabled;
    }

    @Override
    public ResponseData<String> reviewTest(final TestOpportunity testOpportunity,
                                           final HttpServletRequest request) throws ReturnStatusException, IOException {
        ResponseData<String> responseData = new ResponseData<>(TDSReplyCode.OK.getCode(), "OK", null);

        if (isLegacyCallsEnabled) {
            responseData = legacyReviewTest(testOpportunity, request);
        }

        if (!isRemoteCallsEnabled) {
            return responseData;
        }

        Optional<ValidationError> maybeValidationError = examRepository.reviewExam(testOpportunity.getOppInstance());
        if (maybeValidationError.isPresent()) {
            tdsLogger.applicationError (maybeValidationError.get().getMessage(), "reviewTest", request, null);
            HttpContext.getCurrentContext()
                .getResponse()
                .sendError(HttpStatus.SC_FORBIDDEN, "Cannot end the test.");

            responseData = null;
        }

        return responseData;
    }

    /**
     * Executes the legacy implementation of review exam logic.
     *
     * @param testOpportunity The {@link tds.student.services.data.TestOpportunity}
     * @param request
     * @return
     * @throws ReturnStatusException
     * @throws IOException
     */
    private ResponseData<String> legacyReviewTest(final TestOpportunity testOpportunity,
                                                  final HttpServletRequest request) throws ReturnStatusException, IOException {
        // get responses
        final TestManager testManager = new TestManager(testOpportunity);
        testManager.LoadResponses(true);

        // check if test length is met
        testManager.CheckIfTestComplete();

        if (!testManager.IsTestLengthMet()) {
            // TODO mpatel - Check to see status and message in response and make sure following works
            final String message = "Review Test: Cannot end test because test length is not met.";
            tdsLogger.applicationError (message, "reviewTest", request, null);
            HttpContext.getCurrentContext()
                .getResponse()
                .sendError(HttpStatus.SC_FORBIDDEN, "Cannot end the test.");

            return null;
        }

        // check if all visible pages are completed
        PageList pageList = testManager.GetVisiblePages();
        if (!pageList.isAllCompleted()) {
            final String message = "Review Test: Cannot end test because all the groups have not been answered.";
            tdsLogger.applicationError (message, "reviewTest", request, null);
            HttpContext.getCurrentContext()
                .getResponse()
                .sendError(HttpStatus.SC_FORBIDDEN, "Cannot end the test.");

            return null;
        }

        // put test in review mode
        OpportunityStatusChange statusChange = new OpportunityStatusChange (OpportunityStatusType.Review, true);
        opportunityService.setStatus (testOpportunity.getOppInstance (), statusChange);

        return new ResponseData<>(TDSReplyCode.OK.getCode(), "OK", null);
    }
}
