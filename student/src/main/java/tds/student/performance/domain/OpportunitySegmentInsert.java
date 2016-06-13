/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/

package tds.student.performance.domain;


import java.sql.Timestamp;

/**
 * Holds fields to insert to opp seg
 */
public class OpportunitySegmentInsert extends OpportunitySegmentProperties {

    private String formKey;
    private String formId;
    private Integer ftItemCnt;
    private String ftItems;
    private String restorePermOn;
    private Timestamp entryApproved;
    private Timestamp exitApproved;
    private String formCohort;
    private Float initialAbility;
    private Float currentAbility;
    private Timestamp dateExited;
    private String itemPool;
    private Integer poolCount;

    public OpportunitySegmentInsert() {
    }

    public OpportunitySegmentInsert(OpportunitySegmentProperties properties) {
        super(properties);
        this.formKey = null;
        this.formId = null;
        this.ftItemCnt = null;
        this.ftItems = null;
        this.restorePermOn = null;
        this.entryApproved = null;
        this.exitApproved = null;
        this.formCohort = null;
        this.initialAbility = null;
        this.currentAbility = null;
        this.dateExited = null;
        this.itemPool = null;
        this.poolCount = null;
    }

    public Float getCurrentAbility() {
        return currentAbility;
    }

    public void setCurrentAbility(Float currentAbility) {
        this.currentAbility = currentAbility;
    }

    public Timestamp getDateExited() {
        return dateExited;
    }

    public void setDateExited(Timestamp dateExited) {
        this.dateExited = dateExited;
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

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public Integer getFtItemCnt() {
        return ftItemCnt;
    }

    public void setFtItemCnt(Integer ftItemCnt) {
        this.ftItemCnt = ftItemCnt;
    }

    public String getFtItems() {
        return ftItems;
    }

    public void setFtItems(String ftItems) {
        this.ftItems = ftItems;
    }

    public Float getInitialAbility() {
        return initialAbility;
    }

    public void setInitialAbility(Float initialAbility) {
        this.initialAbility = initialAbility;
    }

    public String getItemPool() {
        return itemPool;
    }

    public void setItemPool(String itemPool) {
        this.itemPool = itemPool;
    }

    public Integer getPoolCount() {
        return poolCount;
    }

    public void setPoolCount(Integer poolCount) {
        this.poolCount = poolCount;
    }

    public String getRestorePermOn() {
        return restorePermOn;
    }

    public void setRestorePermOn(String restorePermOn) {
        this.restorePermOn = restorePermOn;
    }

    @Override
    public String toString() {
        return "OpportunitySegmentInsert{" +
                "currentAbility=" + currentAbility +
                ", formKey='" + formKey + '\'' +
                ", formId='" + formId + '\'' +
                ", ftItemCnt=" + ftItemCnt +
                ", ftItems='" + ftItems + '\'' +
                ", restorePermOn='" + restorePermOn + '\'' +
                ", entryApproved=" + entryApproved +
                ", exitApproved=" + exitApproved +
                ", formCohort='" + formCohort + '\'' +
                ", initialAbility=" + initialAbility +
                ", dateExited=" + dateExited +
                ", itemPool='" + itemPool + '\'' +
                ", poolCount=" + poolCount +
                '}' + super.toString();
    }
}
