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
 * Created by emunoz on 12/30/15.
 */
public class UnfinishedResponsePage {
    private Integer page;
    private Integer groupRequired;
    private Integer numItems;
    private Integer validCount;
    private Integer requiredItems;
    private Integer requiredResponses;
    private Boolean isVisible;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getGroupRequired() {
        return groupRequired;
    }

    public void setGroupRequired(Integer groupRequired) {
        this.groupRequired = groupRequired;
    }

    public Integer getNumItems() {
        return numItems;
    }

    public void setNumItems(Integer numItems) {
        this.numItems = numItems;
    }

    public Integer getValidCount() {
        return validCount;
    }

    public void setValidCount(Integer validCount) {
        this.validCount = validCount;
    }

    public Integer getRequiredItems() {
        return requiredItems;
    }

    public void setRequiredItems(Integer requiredItems) {
        this.requiredItems = requiredItems;
    }

    public Integer getRequiredResponses() {
        return requiredResponses;
    }

    public void setRequiredResponses(Integer requiredResponses) {
        this.requiredResponses = requiredResponses;
    }

    public Boolean getVisible() {
        return isVisible;
    }

    public void setVisible(Boolean visible) {
        isVisible = visible;
    }

}
