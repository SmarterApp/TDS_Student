package tds.student.performance.domain;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by jjohnson on 1/2/16.
 */
public class TestSegmentItem {
    private UUID opportunityKey;
    private String segmentKey;
    private Integer segmentPosition;
    private String formKey;
    private String formId;
    private String algorithm;
    private Integer opItemCount;
    private Integer ftItemCount;
    private String ftItems;
    private Integer isPermeable;
    private String restorePermOn;
    private String segmentId;
    private Timestamp entryApproved;
    private Timestamp exitApproved;
    private String formCohort;
    private Boolean isSatisfied;
    private Float initialAbility;
    private Float currentAbility;
    private Timestamp currentDate;
    private Timestamp dateExited;
    private String itemPool;
    private Integer itemPoolCount;

    public UUID getOpportunityKey() {
        return opportunityKey;
    }

    public void setOpportunityKey(UUID opportunityKey) {
        this.opportunityKey = opportunityKey;
    }

    /**
     * Referred to as and relates to {@code testKey} on the {@code TestOpportunity} object.
     */
    public String getSegmentKey() {
        return segmentKey;
    }

    /**
     * Referred to as and relates to {@code testKey} on the {@code TestOpportunity} object.
     */
    public void setSegmentKey(String segment) {
        this.segmentKey = segment;
    }

    public Integer getSegmentPosition() {
        return segmentPosition;
    }

    public void setSegmentPosition(Integer segmentPosition) {
        this.segmentPosition = segmentPosition;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Referred to as {@code maxitems} in the database (@code itembank.tblsetofadminsubjects} and
     * {@code session.sim_segment}.
     */
    public Integer getOpItemCount() {
        return opItemCount;
    }

    /**
     * Referred to as {@code maxitems} in the database (@code itembank.tblsetofadminsubjects} and
     * {@code session.sim_segment}.
     */
    public void setOpItemCount(Integer opItemCount) {
        this.opItemCount = opItemCount;
    }

    public Integer getFtItemCount() {
        return ftItemCount;
    }

    public void setFtItemCount(Integer ftItemCount) {
        this.ftItemCount = ftItemCount;
    }

    public String getFtItems() {
        return ftItems;
    }

    public void setFtItems(String ftItems) {
        this.ftItems = ftItems;
    }

    public Integer getIsPermeable() {
        return isPermeable;
    }

    public void setIsPermeable(Integer isPermeable) {
        this.isPermeable = isPermeable;
    }

    public String getRestorePermOn() {
        return restorePermOn;
    }

    public void setRestorePermOn(String restorePermOn) {
        this.restorePermOn = restorePermOn;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public Timestamp getEntryApproved() {
        return entryApproved;
    }

    public void setEntryApproved(Timestamp entryApproved) {
        this.entryApproved = entryApproved;
    }

    public Timestamp getExitApproved() {
        return exitApproved;
    }

    public void setExitApproved(Timestamp exitApproved) {
        this.exitApproved = exitApproved;
    }

    public String getFormCohort() {
        return formCohort;
    }

    public void setFormCohort(String formCohort) {
        this.formCohort = formCohort;
    }

    public Boolean getIsSatisfied() {
        return isSatisfied;
    }

    public void setIsSatisfied(Boolean isSatisfied) {
        this.isSatisfied = isSatisfied;
    }

    public Float getInitialAbility() {
        return initialAbility;
    }

    public void setInitialAbility(Float initialAbility) {
        this.initialAbility = initialAbility;
    }

    public Float getCurrentAbility() {
        return currentAbility;
    }

    public void setCurrentAbility(Float currentAbility) {
        this.currentAbility = currentAbility;
    }

    public Timestamp getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Timestamp currentDate) {
        this.currentDate = currentDate;
    }

    public Timestamp getDateExited() {
        return dateExited;
    }

    public void setDateExited(Timestamp dateExited) {
        this.dateExited = dateExited;
    }

    public String getItemPool() {
        return itemPool;
    }

    public void setItemPool(String itemPool) {
        this.itemPool = itemPool;
    }

    public Integer getItemPoolCount() {
        return itemPoolCount;
    }

    public void setItemPoolCount(Integer itemPoolCount) {
        this.itemPoolCount = itemPoolCount;
    }
}
