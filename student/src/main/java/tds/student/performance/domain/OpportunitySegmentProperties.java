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
import java.util.UUID;

/**
 * Holds properties from item bank queried to populate a new segment
 */
public class OpportunitySegmentProperties {

    private UUID _fk_TestOpportunity;
    private String _efk_Segment;
    private String segmentId;
    private Integer segmentPosition;
    private String algorithm;
    private Integer opItemCnt;
    private Integer isPermeable;
    private Boolean isSatisfied;
    private Timestamp _date;

    public OpportunitySegmentProperties() {
    }

    public OpportunitySegmentProperties(OpportunitySegmentProperties properties) {
        this._fk_TestOpportunity = properties._fk_TestOpportunity;
        this._efk_Segment = properties._efk_Segment;
        this.segmentId = properties.segmentId;
        this.segmentPosition = properties.segmentPosition;
        this.algorithm = properties.algorithm;
        this.opItemCnt = properties.opItemCnt;
        this.isPermeable = properties.isPermeable;
        this.isSatisfied = properties.isSatisfied;
        this._date = properties._date;
    }

    public Timestamp get_date() {
        return _date;
    }

    public void set_date(Timestamp _date) {
        this._date = _date;
    }

    public String get_efk_Segment() {
        return _efk_Segment;
    }

    public void set_efk_Segment(String _efk_Segment) {
        this._efk_Segment = _efk_Segment;
    }

    public UUID get_fk_TestOpportunity() {
        return _fk_TestOpportunity;
    }

    public void set_fk_TestOpportunity(UUID _fk_TestOpportunity) {
        this._fk_TestOpportunity = _fk_TestOpportunity;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Integer getIsPermeable() {
        return isPermeable;
    }

    public void setIsPermeable(Integer isPermeable) {
        this.isPermeable = isPermeable;
    }

    public Boolean getSatisfied() {
        return isSatisfied;
    }

    public void setSatisfied(Boolean satisfied) {
        isSatisfied = satisfied;
    }

    public Integer getOpItemCnt() {
        return opItemCnt;
    }

    public void setOpItemCnt(Integer opItemCnt) {
        this.opItemCnt = opItemCnt;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public Integer getSegmentPosition() {
        return segmentPosition;
    }

    public void setSegmentPosition(Integer segmentPosition) {
        this.segmentPosition = segmentPosition;
    }

    @Override
    public String toString() {
        return "OpportunitySegmentProperties{" +
                "_date=" + _date +
                ", _fk_TestOpportunity=" + _fk_TestOpportunity +
                ", _efk_Segment='" + _efk_Segment + '\'' +
                ", segmentId='" + segmentId + '\'' +
                ", segmentPosition=" + segmentPosition +
                ", algorithm='" + algorithm + '\'' +
                ", opItemCnt=" + opItemCnt +
                ", isPermeable=" + isPermeable +
                ", isSatisfied=" + isSatisfied +
                '}';
    }
}
