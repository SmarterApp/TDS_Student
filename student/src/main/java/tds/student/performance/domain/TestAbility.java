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
import java.util.Date;
import java.util.UUID;

/**
 * Created by emunoz on 12/28/15.
 */
public class TestAbility {
    private UUID oppkey;

    private String test;

    private Integer opportunity;

    private Timestamp dateScored;

    private Float score;

    public UUID getOppkey() {
        return oppkey;
    }

    public void setOppkey(UUID oppkey) {
        this.oppkey = oppkey;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public Integer getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Integer opportunity) {
        this.opportunity = opportunity;
    }

    public Timestamp getDateScored() {
        return dateScored;
    }

    public void setDateScored(Timestamp dateScored) {
        this.dateScored = dateScored;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }
}


