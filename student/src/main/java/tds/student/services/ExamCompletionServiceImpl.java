package tds.student.services;

import AIR.Common.Web.TDSReplyCode;
import AIR.Common.data.ResponseData;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import tds.exam.ExamStatusCode;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.OpportunityStatusChange;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.web.TestManager;

@Service
@Scope("prototype")
public class ExamCompletionServiceImpl implements ExamCompletionService {
    private final IOpportunityService opportunityService;
    private final boolean isLegacyCallsEnabled;

    @Autowired
    public ExamCompletionServiceImpl(@Qualifier("integrationOpportunityService") final IOpportunityService opportunityService,
                                     @Value("${tds.exam.legacy.enabled}") final boolean legacyCallsEnabled,
                                     @Value("${tds.exam.remote.enabled}") final boolean remoteExamCallsEnabled) {
        if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
            throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration for 'tds.exam.remote.enabled' and 'tds.exam.legacy.enabled' settings");
        }

        this.opportunityService = opportunityService;
        isLegacyCallsEnabled = legacyCallsEnabled;
    }

    @Override
    public ResponseData<String> updateStatusWithValidation(final TestOpportunity testOpportunity,
                                                           final TestManager testManager,
                                                           final String statusCode) throws ReturnStatusException {
        if (isLegacyCallsEnabled) {
            if (!ExamStatusCode.STATUS_REVIEW.equals(statusCode) && !ExamStatusCode.STATUS_COMPLETED.equals(statusCode)) {
                throw new IllegalArgumentException("Can only call the legacy method with a status code of 'review' or 'completed'.");
            }
        }

        // get responses
        testManager.LoadResponses(true);

        // check if test is complete
        // NOTE:  The legacy version of this code would call testManager.CheckIfTestComplete() and ignore the result.  A
        // call to testManager.IsTestLengthMet() would execute immediately afterward, which checks the _isComplete
        // field of the testManager.  Since the _isComplete field is set by testManager.CheckIfTestComplete(), the call
        // to testManager.IsTestLengthMet() has been ommitted (because testManager.CheckIfTestComplete() and
        // testManager.IsTestLengthMet() effectively return the same result)
        if (!testManager.CheckIfTestComplete()) {
            return new ResponseData<>(TDSReplyCode.Error.getCode(),
                "Review Test: Cannot end test because test length is not met.",
                null);
        }

        // check if all visible pages are completed
        if (!testManager.GetVisiblePages().isAllCompleted()) {
            return new ResponseData<>(TDSReplyCode.Error.getCode(),
                "Review Test: Cannot end test because all the groups have not been answered.",
                null);
        }

        // put test in review mode
        final OpportunityStatusChange statusChange =
            new OpportunityStatusChange(OpportunityStatusType.parse(statusCode), true);
        opportunityService.setStatus(testOpportunity.getOppInstance(), statusChange);

        return new ResponseData<>(TDSReplyCode.OK.getCode(), "OK", null);
    }
}
