package tds.student.performance.domain;

import tds.student.sql.data.OpportunityInstance;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
Represents a single record in the {@code session.testopportunity} database.
 */
public class TestOpportunity {
    private UUID key;
    private UUID sessionKey;
    private UUID browserKey;
    private String testKey;
    private Long testee;
    private String testId;
    private Integer opportunity;
    private String status;
    private Timestamp dateStarted;
    private Timestamp dateRestarted;
    private Timestamp dateChanged;
    private Timestamp expireFrom;
    private String stage;
    private Integer restartCount;
    private Integer gracePeriodRestarts;
    private Integer maxItems;
    private String subject;
    private String clientName;
    private Boolean isSegmented;
    private String algorithm;
    private String environment;
    private Integer simulationSegmentCount;
    private Integer waitingForSegment;

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public UUID getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(UUID sessionKey) {
        this.sessionKey = sessionKey;
    }

    public UUID getBrowserKey() {
        return browserKey;
    }

    public void setBrowserKey(UUID browserKey) {
        this.browserKey = browserKey;
    }

    public String getTestKey() {
        return testKey;
    }

    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }

    public Long getTestee() {
        return testee;
    }

    public void setTestee(Long testee) {
        this.testee = testee;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public Integer getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Integer opportunity) {
        this.opportunity = opportunity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Timestamp dateStarted) {
        this.dateStarted = dateStarted;
    }

    public Timestamp getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(Timestamp dateChanged) {
        this.dateChanged = dateChanged;
    }

    /**
     * Referred to as {@code rcnt} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @return {@code Integer } representing the restart count of the {@code TestOpportunity}.
     */
    public Integer getRestartCount() {
        return restartCount;
    }

    /**
     * Referred to as {@code rcnt} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @param restartCount {@code Integer } representing the restart count of the {@code TestOpportunity}.
     */
    public void setRestartCount(Integer restartCount) {
        this.restartCount = restartCount;
    }

    /**
     * Referred to as {@code gpRestarts} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @return {@code Integer } representing the restart count of the {@code TestOpportunity}.
     */
    public Integer getGracePeriodRestarts() {
        return gracePeriodRestarts;
    }

    public void setGracePeriodRestarts(Integer gracePeriodRestarts) {
        this.gracePeriodRestarts = gracePeriodRestarts;
    }

    /**
     * Referred to as {@code testlength} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @return {@code Integer } representing the maximum number of items in the {@code TestOpportunity}.
     */
    public Integer getMaxItems() {
        return maxItems;
    }

    /**
     * Referred to as {@code testlength} in {@code StudentDLL.T_StartTestOpportunity_SP}.
     * @param maxItems  {@code Integer } representing the maximum number of items in the {@code TestOpportunity}.
     */
    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Boolean getIsSegmented() {
        return isSegmented;
    }

    public void setIsSegmented(Boolean isSegmented) {
        this.isSegmented = isSegmented;
    }

    public Timestamp getExpireFrom() {
        return expireFrom;
    }

    public void setExpireFrom(Timestamp expireFrom) {
        this.expireFrom = expireFrom;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * This value comes from joining the {@code session.testopportunity} table to the {@code session._externs} table.
     * <p>
     *     If the {@code TestOpportunity}'s environment is set to "SIMULATION", then this {@code TestOpportunity}
     *     <em>might</em> be a simulation.  Refer to the {@code IsSimulation} method below.
     * </p>
     * @return A {@code String} describing the environment to which this {@code TestOpportunity}'s client belongs.
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * This value comes from joining the {@code session.testopportunity} table to the {@code session._externs} table.
     * <p>
     *     If the {@code TestOpportunity}'s environment is set to "SIMULATION", then this {@code TestOpportunity}
     *     <em>might</em> be a simulation.  Refer to the {@code IsSimulation} method below.
     * </p>
     * @param environment  A {@code String} describing the environment to which this {@code TestOpportunity}'s client
     *                     belongs.
     */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * The number of records this {@code TestOpportunity} has in the {@code session.sim_segment} table.
     * <p>
     *     If this number is greater than 0, then this {@code TestOpportunity} <em>might</em> be a simulation.  See the
     *     {@code isSimulation} method below for how to determine if a {@code TestOpportunity} is a simulation.  This
     *     rule was found in {@code StudentDLL.IsSimulation_FN} on line 3000.
     * </p>
     * @return {@code Integer} that is a count of the rows in {@code session.sim_segment} for this {@code TestOpportunity}
     */
    public Integer getSimulationSegmentCount() {
        return simulationSegmentCount;
    }

    /**
     * The number of records this {@code TestOpportunity} has in the {@code session.sim_segment} table.
     * <p>
     *     If this number is greater than 0, then this {@code TestOpportunity} <em>might</em> be a simulation.  See the
     *     {@code isSimulation} method below for how to determine if a {@code TestOpportunity} is a simulation.  This
     *     rule was found in {@code StudentDLL.IsSimulation_FN} on line 3000.
     * </p>
     * @param simulationSegmentCount  {@code Integer} that is a count of the rows in {@code session.sim_segment} for
     *                                this {@code TestOpportunity}
     */
    public void setSimulationSegmentCount(Integer simulationSegmentCount) {
        this.simulationSegmentCount = simulationSegmentCount;
    }

    /**
     * Determine if this {@code TestOpportunity} is a simulation.
     * <p>
     *     This method emulates the logic stored in {@code StudentDLL.IsSimulation_FN}.  The rule in
     *     {@code StudentDLL.IsSimulation_FN} states:
     *
     *     * The {@code TestOpportunity}'s environment must be set to "SIMULATION"
     *     * There must be records in the {@code session.sim_segment} table for the {@code TestOpportunity}'s session key
     *       AND the {@code TestOpportunity}'s admin subject.
     *
     *     The second bullet point is handled by the query in {@code TestOpportunityDaoImpl} that fetches the
     *     {@code TestOpportunity} record.
     * </p>
     * @return {@code True} if the {@code TestOpportunity} is a simulation (that is, all of the conditions above are
     * satisifed); otherwise {@code False}.
     */
    public Boolean isSimulation() {
        return this.getClientName().toLowerCase().equals("simulation") && this.getSimulationSegmentCount() > 0;
    }

    public Timestamp getDateRestarted() {
        return dateRestarted;
    }

    public void setDateRestarted(Timestamp dateRestarted) {
        this.dateRestarted = dateRestarted;
    }

    public Integer getWaitingForSegment() {
        return waitingForSegment;
    }

    public void setWaitingForSegment(Integer waitingForSegment) {
        this.waitingForSegment = waitingForSegment;
    }
}
