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


