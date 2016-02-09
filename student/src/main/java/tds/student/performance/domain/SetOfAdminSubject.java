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

/**
 * Represents a record from the {@code itembank.tblsetofadminsubjects} table.
 * <p>
 *     <strong>NOTE:</strong> This record is by no means complete; it just have enough data to allow the
 *     {@code TestOpportunityService.startTestOpportunity} to run.
 * </p>
 */
public class SetOfAdminSubject {
    private String key;
    private Integer maxItems; // Identified as operationalLength in StudentDLL.T_StartTestOpportunity_SP
    private Float startAbility;
    private String testId;
    private Boolean isSegmented;
    private String selectionAlgorithm;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    public Float getStartAbility() {
        return startAbility;
    }

    public void setStartAbility(Float startAbility) {
        this.startAbility = startAbility;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public Boolean getSegmented() {
        return isSegmented;
    }

    public void setSegmented(Boolean segmented) {
        isSegmented = segmented;
    }

    public String getSelectionAlgorithm() {
        return selectionAlgorithm;
    }

    public void setSelectionAlgorithm(String selectionAlgorithm) {
        this.selectionAlgorithm = selectionAlgorithm;
    }
}
