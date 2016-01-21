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
