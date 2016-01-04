package tds.student.performance.services.impl;

import AIR.Common.DB.SQLConnection;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.dll.api.ICommonDLL;
import tds.dll.api.IStudentDLL;
import tds.student.performance.dao.*;
import tds.student.performance.domain.*;
import tds.student.performance.exceptions.ReturnErrorException;
import tds.student.performance.services.*;
import tds.student.performance.utils.DateUtility;
import tds.student.performance.utils.HostNameHelper;
import tds.student.performance.utils.LegacySqlConnection;
import tds.student.performance.utils.DateUtils;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.TestConfig;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A service for interacting with a {@code TestOpportunity}.
 */
@Service
public class TestOpportunityServiceImpl implements TestOpportunityService {
    private static final Logger logger = LoggerFactory.getLogger(TestOpportunityServiceImpl.class);

    @Autowired
    DbLatencyService dbLatencyService;

    @Autowired
    TestOpportunityDao testOpportunityDao;

    @Autowired
    SessionAuditDao sessionAuditDao;

    @Autowired
    TestOpportunityAuditDao testOpportunityAuditDao;

    @Autowired
    TesteeResponseDao testeeResponseDao;

    @Autowired
    TestAbilityDao testAbilityDao;

    // TODO:  Replace with calls to configurationService; no need to wire up both Dao and Service.
    @Autowired
    ConfigurationDao configurationDao;

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    ItemBankDao itemBankDao;

    @Autowired
    TestSegmentDao testSegmentDao;

    @Autowired
    TestSessionService testSessionService;

    @Autowired
    DateUtility dateUtility;

    @Autowired
    LegacyErrorHandlerService legacyErrorHandlerService;
    
    @Autowired
    IStudentDLL legacyStudentDll;

    @Autowired
    ICommonDLL legacyCommonDll;

    @Autowired
    LegacySqlConnection legacySqlConnection;

    @Override
    public TestConfig startTestOpportunity(OpportunityInstance opportunityInstance, String testKey, String formKeyList) {
        Date start = new Date();
        TestConfig config = new TestConfig();

        try {
            TestOpportunity testOpportunity = testOpportunityDao.get(opportunityInstance.getKey());
            if (testOpportunity == null) {
                String msg = String.format("No TestOpportunity for key %s", opportunityInstance.getKey());
                legacyErrorHandlerService.logDbError("T_StartTestOpportunity", msg, null, null, null, opportunityInstance.getKey());
                legacyErrorHandlerService.throwReturnErrorException(null, "T_StartTestOpportunity", msg, null, opportunityInstance.getKey(), null, "failed");
            }

            TestSession testSession = testSessionService.get(opportunityInstance.getSessionKey());
            if (testSession == null) {
                String msg = String.format("Could not find TestSession record in session.session for key %s", opportunityInstance.getSessionKey());
                legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "T_StartTestOpportunity", msg, null, testOpportunity.getKey(), null, "failed");
            }

            verifyTesteeAccess(testOpportunity, testSession, opportunityInstance);

            TestSessionTimeLimitConfiguration timelimitConfiguration = testSessionService.getTimelimitConfiguration(
                    testOpportunity.getClientName(),
                    testOpportunity.getTestId());
            if (testSession == null) {
                String msg = String.format("Could not find record in session.timelimits for clientname %s, testId %s", testOpportunity.getClientName(), testOpportunity.getTestId());
                legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "T_StartTestOpportunity", msg, null, testOpportunity.getKey(), null, "failed");
            }

            ClientTestProperty clientTestProperty = configurationDao.getClientTestProperty(
                    testOpportunity.getClientName(),
                    testOpportunity.getTestId());
            if (clientTestProperty == null) {
                String msg = String.format("Could not find record in configs.client_testproperties for clientname %s, testId %s", testOpportunity.getClientName(), testOpportunity.getTestId());
                legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "T_StartTestOpportunity", msg, null, testOpportunity.getKey(), null, "failed");
            }

            if (!testOpportunity.getStatus().toLowerCase().equals("approved")) {
                logger.error(String.format("Test %s for opportunity %s start/restart not approved by test administrator", testOpportunity.getTestId(), testOpportunity.getKey()));
                legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "T_StartTestOpportunity", "Test start/restart not approved by test administrator", null, testOpportunity.getKey(), "T_StartTestOpportunity", "denied");
            }

            Boolean scoreByTds = configurationDao.isSetForScoreByTDS(
                    testOpportunity.getClientName(),
                    testOpportunity.getTestId());

            if (testOpportunity.getDateStarted() == null) {
                // Emulate logic to call legacy method (StudentDLL._InitializeOpportunity_SP) on line 5326
                Integer testLength = initializeStudentOpportunity(testOpportunity, formKeyList);

                config = TestConfigHelper.getNew(clientTestProperty, timelimitConfiguration, testLength, scoreByTds);
            } else {         // Restart the most recent test opportunity, starting @ line 5373 - StudentDLL
                Date lastActivity = testOpportunityDao.getLastActivity(opportunityInstance.getKey());
                Integer gracePeriodRestarts = testOpportunity.getGracePeriodRestarts();
                Integer restartCount = testOpportunity.getRestartCount();

                //TODO: Replace below with db time
                // Timestamp now = new Timestamp(System.currentTimeMillis());
                Timestamp now = new Timestamp(dateUtility.getDbDate().getTime());
                boolean isTimeDiffLessThanDelay =
                        (DateUtils.minutesDiff(lastActivity, start) < timelimitConfiguration.getOpportunityDelay());

                if(isTimeDiffLessThanDelay) {
                    gracePeriodRestarts++;
                }

                testOpportunity.setRestartCount(restartCount + 1);
                testOpportunity.setStatus("started");
                testOpportunity.setGracePeriodRestarts(gracePeriodRestarts);
                testOpportunity.setDateChanged(now);
                testOpportunity.setDateStarted(now);
                testOpportunityDao.update(testOpportunity);

                TestOpportunityAudit oppAudit = new TestOpportunityAudit();
                oppAudit.setTestOpportunityKey(opportunityInstance.getKey());
                oppAudit.setSessionKey(opportunityInstance.getSessionKey());
                oppAudit.setHostName(legacyCommonDll.getLocalhostName());
                oppAudit.setBrowserKey(opportunityInstance.getBrowserKey());
                oppAudit.setDatabaseName("session");
                oppAudit.setDateAccessed(new Timestamp(System.currentTimeMillis()));
                oppAudit.setAccessType("restart " + (restartCount + 1));
                testOpportunityAuditDao.create(oppAudit);

                if (testSession.getSessionType() == 1) {
                    testeeResponseDao.updateRestartCount(opportunityInstance.getKey(), restartCount, false);
                } else if (isTimeDiffLessThanDelay) {
                    testeeResponseDao.updateRestartCount(opportunityInstance.getKey(), restartCount, true);
                } else if (clientTestProperty.getDeleteUnansweredItems()) {
                    removeUnanswered(testOpportunity);
                }

                restartCount++;

                //Call _UnfinishedResponsePages_SP (connection, oppKey, rcnt, true) equivalent
                updateUnfinishedResponsePages(opportunityInstance.getKey(), restartCount);

                config = TestConfigHelper.getRestart(
                        clientTestProperty,
                        timelimitConfiguration,
                        testOpportunity.getMaxItems(),
                        restartCount,
                        0, // TODO:  Need to set restartPosition properly.
                        scoreByTds);
            }

            dbLatencyService.logLatency("T_StartTestOpportunity", start, null, testOpportunity);
        } catch (ReturnErrorException e) {
            logger.error(e.getMessage(), e);
            legacyErrorHandlerService.logDbError("T_StartTestOpportunity", e.getMessage(), null, testKey, null, opportunityInstance.getKey());

            // The legacy OpportunityStatus.startTest method looks for the TestConfig.getReturnStatus to determine
            // if the call was successful or not.
            ReturnStatus failureStatus = new ReturnStatus();
            failureStatus.setStatus(e.getStatus());
            failureStatus.setReason(e.getReason());
            failureStatus.setContext(e.getContext());
            failureStatus.setAppKey(e.getAppKey());

            config.setReturnStatus(failureStatus);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            legacyErrorHandlerService.logDbError("T_StartTestOpportunity", e.getMessage(), null, testKey, null, opportunityInstance.getKey());

            // The legacy OpportunityStatus.startTest method looks for the TestConfig.getReturnStatus to determine
            // if the call was successful or not.
            ReturnStatus failureStatus = new ReturnStatus();
            failureStatus.setStatus("failed");
            failureStatus.setReason(e.getMessage());
            failureStatus.setAppKey(opportunityInstance.getKey().toString());

        } catch (ReturnStatusException e) {
            logger.error(e.getMessage(), e);
            legacyErrorHandlerService.logDbError("T_StartTestOpportunity", e.getMessage(), null, testKey, null, opportunityInstance.getKey());

            // The legacy OpportunityStatus.startTest method looks for the TestConfig.getReturnStatus to determine
            // if the call was successful or not.
            ReturnStatus failureStatus = new ReturnStatus();
            failureStatus.setStatus(e.getReturnStatus().getStatus());
            failureStatus.setReason(e.getMessage());
            failureStatus.setAppKey(e.getReturnStatus().getAppKey());
        }

        return config;
    }

    /**
     * This method emulated the functionality and logic contained in {@code StudentDll._UnfinishedResponsePages_SP}.
     *
     * NOTE: The legacy _UnfinishedResponsePages_SP call has a return value that never appears to be read,
     * at least not by student or the tdsdll project. The original method also has a doUpdate flag that
     * seems to be only set to "true" in every instance that the legacy method is called in production code.
     * Because of this, the flag and option select branch has been removed and the method has a void return value.
     *
     * @param oppKey
     * @param newRestartCount
     */
    public void updateUnfinishedResponsePages(UUID oppKey, Integer newRestartCount){
        List<UnfinishedResponsePage> pages = testeeResponseDao.getUnfinishedPages(oppKey);

        for (UnfinishedResponsePage page : pages) {
            if (page.getGroupRequired() == -1) {
                page.setGroupRequired(page.getNumItems());
            }

            if (page.getRequiredResponses() < page.getRequiredItems() ||
                    page.getValidCount() < page.getGroupRequired()) {
                page.setVisible(true);
                testeeResponseDao.updateRestartCount(oppKey, newRestartCount, false);
            }
        }

    }

    /**
     * This method emulates the functionality and logic contained in {@code StudentDLL._GetInitialAbility_SP}.
     *
     * @param opportunity the opportunity key to check the ability for
     */
    @Override
    public Float getInitialAbility(TestOpportunity opportunity, ClientTestProperty property) {
        Date start = new Date();
        Float ability = null;
        Boolean bySubject = false;
        Double slope = null;
        Double intercept = null;

        if (property != null) {
            bySubject = property.getInitialAbilityBySubject();
            slope = property.getAbilitySlope();
            intercept = property.getAbilityIntercept();
        }

        List<TestAbility> testAbilities = testAbilityDao.getTestAbilities(opportunity.getKey(), opportunity.getClientName(),
                opportunity.getSubject(), opportunity.getTestee().longValue());
        TestAbility initialAbility = getMostRecentTestAbility(testAbilities, opportunity.getTestKey(), false);

        //First, try to get the ability for current subject/test
        if (initialAbility != null) {
            ability = initialAbility.getScore();
        } else if (bySubject) { // If that didn't retrieve anything, get the ability from the same subject, any test
            initialAbility = getMostRecentTestAbility(testAbilities, opportunity.getTestKey(), true);
            if (initialAbility != null) {
                ability = initialAbility.getScore();
            } else { // and if that didn't work, get the initial ability from the previous year.
                Float initAbilityFromHistory = testAbilityDao.getMostRecentTestAbilityFromHistory(opportunity.getClientName(),
                        opportunity.getSubject(), opportunity.getTestee().longValue());

                if (initAbilityFromHistory != null && slope != null && intercept != null) {
                    ability = initAbilityFromHistory * slope.floatValue() + intercept.floatValue();
                } else if (initAbilityFromHistory != null){
                    ability = initAbilityFromHistory;
                }
            }
        }

        //If the ability was not retrieved/set from the above logic, grab it from the item bank DB
        if (ability == null) {
            SetOfAdminSubject subject = itemBankDao.get(opportunity.getTestKey());
            if (subject != null) {
                ability = subject.getStartAbility().floatValue();
            } else {
                logger.warn("Could not set the ability for oppKey " + opportunity.getKey());
            }
        }

        dbLatencyService.logLatency("GetInitialAbility", start, null, opportunity);
        return ability;
    }

    /**
     * Gets the most recent {@link TestAbility} based on the dateScored value
     *
     * @param testAbilityList the list of {@link TestAbility}s to iterate through
     * @param test  The test key
     * @param inverse Specifies whether to search for matches or non-matches of the test key
     * @return
     */
    private TestAbility getMostRecentTestAbility(List<TestAbility> testAbilityList, String test, boolean inverse) {
        TestAbility mostRecentAbility = null;

        for (TestAbility ability : testAbilityList) {
            if (inverse) {
                if (!test.equals(ability.getTest())) {
                    if (mostRecentAbility == null || mostRecentAbility.getDateScored().before(ability.getDateScored())) {
                        mostRecentAbility = ability;
                    }
                }
            } else {
                if (test.equals(ability.getTest())) {
                    if (mostRecentAbility == null || mostRecentAbility.getDateScored().before(ability.getDateScored())) {
                        mostRecentAbility = ability;
                    }
                }
            }
        }

        return mostRecentAbility;
    }

    /**
     * Set up a {@link TestOpportunity} to start for the first time.
     * <p>
     *     This method currently wraps a call to the {@code StudentDLL._InitializeOpportunity_SP} in the legacy
     *     codebase.
     * </p>
     *
     * @param testOpportunity The {@link TestOpportunity} to initialize.
     * @param formKeyList A list of form keys passed in from the caller.
     * @return The total number of items in the test.
     */
    private Integer initializeStudentOpportunity(TestOpportunity testOpportunity, String formKeyList) throws ReturnErrorException, SQLException, ReturnStatusException {
        SQLConnection legacyConnection = legacySqlConnection.get();
        _Ref<Integer> testLength = new _Ref<>();
        _Ref<String> reason = new _Ref<>();

        legacyStudentDll._InitializeOpportunity_SP(
                legacyConnection,
                testOpportunity.getKey(),
                testLength,
                reason,
                formKeyList);

        if (reason.get() != null) {
            legacyErrorHandlerService.logDbError("T_StartTestOpportunity", reason.get(), testOpportunity.getTestee(), testOpportunity.getTestId(), null, testOpportunity.getKey());
            legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "T_StartTestOpportunity", reason.get(), null, testOpportunity.getKey(), "_InitializeOpportunity", "failed");
        }

        testOpportunityAuditDao.create(new TestOpportunityAudit(
                testOpportunity.getKey(),
                new Timestamp(new Date().getTime()),
                "started",
                testOpportunity.getSessionKey(),
                HostNameHelper.getHostName(),
                "session"));

        return testLength.get();
    }

    private void initializeTestSegments(TestOpportunity testOpportunity, TestSession testSession, String formKeyList) throws SQLException, ReturnErrorException, ReturnStatusException {
        List<TestSegmentItem> segmentItems;
        if (testOpportunity.isSimulation()) { // Get segments for the simulation (StudentDLL._InitializeSegments_SP @ line 4589)
            segmentItems = testSegmentDao.getForSimulation(testOpportunity);
            // NOTE: can use testOpportunity.getSessionKey() in place of sessionPoolKey (StudentDLL._InitializeSegments_SP @ line 4611)
        } else if (testOpportunity.getIsSegmented()) { // Get segments for the segment (StudentDLL._InitializeSegments_SP @ line 4598)
            segmentItems = testSegmentDao.getSegmented(testOpportunity);
        } else { // Get segments for un-segmented opportunity (StudentDLL._InitializeSegments_SP @ line 4604)
            segmentItems = testSegmentDao.get(testOpportunity);
        }

        // Find the maximum segment position (this should be length of list - 1, but who knows?)
        // TODO: Is this find even necessary?  Looping through each item in the results should be sufficient.

        // LOOP through each segment fetched during the queries above:
        for (TestSegmentItem segmentItem : segmentItems) {
            // IF algorithm == "fixedform":
            if (segmentItem.getAlgorithm().toLowerCase().equals("fixedform")) {
                // CALL _SelectTestForm_Driver_SP
                String testFormDriverResponse = configurationService.selectTestFormDriver(
                        testOpportunity,
                        testSession,
                        formKeyList);

                // IF the formKeyRef value that comes back from _SelectTestForm_Driver_SP == null
                if (testFormDriverResponse == null ) {
                    // EXCEPTION: return empty record set and set error reason to "Unable to complete test form selection"
                    legacyErrorHandlerService.logDbError("T_StartTestOpportunity", "Did not find formKeyRef", testOpportunity.getTestee(), testOpportunity.getTestId(), null, testOpportunity.getKey());
                    legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "T_StartTestOpportunity", "Did not find formKeyRef", null, testOpportunity.getKey(), "_InitializeOpportunity", "failed");
                }


                // set poolCountRef to whatever formLengthRef value is.
                // IF formCohort == null:
                    // get cohort from itembank.testform
            } else {
                // CALL StudentDLL._ComputeSegmentPool_SP
                // CALL FT_IsEligible_FN to get isEligible value
                // IF isEligible == 1 and newlenRef == opitems
                    // CALL _FT_SelectItemgroups_SP
                // ELSE:
                    // ftcntRef = 0
            }

            // update current record to new values
        }



            // CALL _SelectTestForm_Driver_SP
            // IF the formKeyRef value that comes back from _SelectTestForm_Driver_SP == null
              // EXCEPTION: return empty record set and set error reason to "Unable to complete test form selection"
           // set poolCountRef to whatever formLengthRef value is.
           // IF formCohort == null:
             // get cohort from itembank.testform
          // ELSE algorithm != "fixedform":
            // CALL StudentDLL._ComputeSegmentPool_SP
            // CALL FT_IsEligible_FN to get isEligible value
            // IF isEligible == 1 and newlenRef == opitems:
              // CALL _FT_SelectItemgroups_SP
           // ELSE:
             // ftcntRef = 0
          // update current record to new values
        // END LOOP (phew!)

        // IF there are no records in list that have opItemCnt + ftItemCnt > 0:
          // EXECPTION: "No items in pool for _InitializeTestSegments"

        // INSERT updated list into session.testopportunitysegment table.
    }

    private void selectTestFormPredetermined(TestOpportunity testOpportunity, String formList) {

    }

    /**
     * Remove unanswered test items from the {@link TestOpportunity}.
     * <p>
     *     This method currently wraps a call to the {@code StudentDLL._RemoveUnanswered_SP} in the legacy codebase.
     * </p>
     * <p>
     *     <strong>NOTE:</strong> The legacy method returns a value (a {@code MultiDataResultSet}), but the return value
     *     is never used by the caller ({@code StudentDLL.T_StartTestOpportunity_SP} in this case).  Therefore the
     *     return value is ignored.
     * </p>
     *
     * @param testOpportunity The {@link TestOpportunity} for which unanswered test items should be removed.
     */
    private void removeUnanswered(TestOpportunity testOpportunity) throws ReturnErrorException {
        try (SQLConnection legacyConnection = legacySqlConnection.get()) {
            legacyStudentDll._RemoveUnanswered_SP(legacyConnection, testOpportunity.getKey());
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            legacyErrorHandlerService.logDbError("T_StartTestOpportunity", e.getMessage(), testOpportunity.getTestee(), testOpportunity.getTestId(), null, testOpportunity.getKey());

            throw new ReturnErrorException("failed", e.getMessage(), "removeUnanswered", e.getMessage());
        } catch (ReturnStatusException e) {
            logger.error(e.getMessage(), e);
            legacyErrorHandlerService.logDbError("T_StartTestOpportunity", e.getMessage(), testOpportunity.getTestee(), testOpportunity.getTestId(), null, testOpportunity.getKey());

            throw new ReturnErrorException("failed", e.getMessage(), "removeUnanswered", e.getMessage());
        }
    }

    /**
     * This method emulates the functionality and logic contained in {@code StudentDLL._ValidateTesteeAccessProc_SP}.
     *
     * @param testOpportunity the {@link TestOpportunity} attempting to start.
     * @param opportunityInstance the {@code OpportunityInstance} attempting to start the {@code TestOpportunity}.
     */
    private void verifyTesteeAccess(TestOpportunity testOpportunity, TestSession testSession, OpportunityInstance opportunityInstance) throws ReturnErrorException, SQLException, ReturnStatusException {
        Date now = dateUtility.getDbDate(); // since this is used to check if the test is open, it needs to match the DB server timezone

        // Emulate logic on line 492 of _ValidateTesteeAccessProc_SP in StudentDLL.class
        // RULE:  The test opportunity's browser key must match the opportunity instance's browser key.
        if (!testOpportunity.getBrowserKey().equals(opportunityInstance.getBrowserKey())) {
            String msg = String.format("testOpportunity.getBrowserKey() %s does not match opportunityInstance.getBrowserKey() %s", testOpportunity.getBrowserKey(), opportunityInstance.getBrowserKey());
            legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "T_StartTestOpportunity", msg, null, testOpportunity.getKey(), "_ValidateTesteeAccess", "denied");
        }

        // Emulate logic on line 555 of _ValidateTesteeAccessProc_SP in StudentDLL.class
        // RULE:  The test opportunity's session key must match the opportunity instance's session key.
        if (!testOpportunity.getSessionKey().equals(opportunityInstance.getSessionKey())) {
            String msg = String.format("testOpportunity.getSessionKey() %s does not match opportunityInstance.getSessionKey() %s", testOpportunity.getSessionKey(), opportunityInstance.getSessionKey());
            legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "T_StartTestOpportunity", msg, null, testOpportunity.getKey(), "_ValidateTesteeAccess", "denied");
        }

        // Emulate logic on line 523 of _ValidateTesteeAccessProc_SP in StudentDLL.class
        // RULE:  The test session must be open in order for the student to start the test.
        if (!testSession.isOpen(now)) {
            String msg = String.format("TestSession.isOpen for session key %s and date %s is false.", testSession.getKey(), now);
            legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "T_StartTestOpportunity", msg, null, testOpportunity.getKey(), "_ValidateTesteeAccess", "denied");
        }

        // Emulate logic on line 529 of _ValidateTesteeAccessProc_SP in StudentDLL.class.  Apparently, having a NULL
        // proctor value for the test session is okay...?  In the legacy method,  _ValidateTesteeAccessProc_SP in StudentDLL.class
        // only fails if the error has some message in it.  This call in the  _ValidateTesteeAccessProc_SP in StudentDLL.class
        // just returns without assigning a message to the error argument.
        if (testSession.getProctorId() == null) {
            logger.warn(String.format("TestSession for session key %s has a NULL proctor value.", testSession.getKey()));
            return;
        }

        // Emulate logic on line 533 of _ValidateTesteeAccessProc_SP in StudentDLL.class.
        Integer checkIn = testSessionService.getCheckInTimeLimit(testSession.getClientName());
        if (checkIn == null || checkIn == 0) {
            String msg = String.format("Check in value for TestSession with client name '%s' %s", testSession.getClientName(), checkIn == null ? "is null" : "is 0");
            legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "T_StartTestOpportunity", msg, null, testOpportunity.getKey(), "_ValidateTesteeAccess", "denied");
        }

        // Emulate logic on line 533 of _ValidateTesteeAccessProc_SP in StudentDLL.class.
        // RULE:  Student should not be able to start a test if the timeout window has expired.
        // NOTE:  Unlike CommonDLL.P_PauseSession_SP, the StudentDLL._ValidateTesteeAccessProc_SP does not check the
        // configs.client_systemflags table to determine if the client is configured to log session audit records.
        // NOTE:  checkIn time is in MINUTES, so need to multiply checkIn by 60,000 milliseconds so the math works out.
        Date dateVisitedPlusCheckIn = new Date(testSession.getDateVisited().getTime() + (checkIn * 60000L));
        if (now.after(dateVisitedPlusCheckIn)) {
            sessionAuditDao.create(new SessionAudit(
                    testSession.getKey(),
                    new Timestamp(now.getTime()),
                    "TACheckin TIMEOUT",
                    HostNameHelper.getHostName(),
                    testSession.getSessionBrowser(),
                    "session"
            ));

            testSessionService.pause(testOpportunity, testSession);

            String msg = String.format("TestSession for session key %s is not available for testing.", testSession.getKey());
            legacyErrorHandlerService.throwReturnErrorException(testOpportunity.getClientName(), "T_StartTestOpportunity", msg, null, testOpportunity.getKey(), "_ValidateTesteeAccess", "denied");
        }
    }
}