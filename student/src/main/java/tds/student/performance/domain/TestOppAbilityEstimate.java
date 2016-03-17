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
 * Created by emunoz on 1/4/16.
 */
public class TestOppAbilityEstimate {
    private UUID oppKey;
    private String strand;
    private Float ability;
    private Integer itemPosition;
    private Timestamp date;

    public TestOppAbilityEstimate() {}

    public TestOppAbilityEstimate(UUID oppKey, String strand, Float ability, Integer itemPos, Timestamp date) {
        this.oppKey = oppKey;
        this.strand = strand;
        this.ability = ability;
        this.itemPosition = itemPos;
        this.date = date;
    }

    public Integer getItemPosition() {
        return itemPosition;
    }

    public void setItemPosition(Integer itemPosition) {
        this.itemPosition = itemPosition;
    }

    public UUID getOppKey() {
        return oppKey;
    }

    public void setOppKey(UUID oppKey) {
        this.oppKey = oppKey;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public Float getAbility() {
        return ability;
    }

    public void setAbility(Float ability) {
        this.ability = ability;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

}
