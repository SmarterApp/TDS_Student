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
    private Double startAbility;

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

    public Double getStartAbility() {
        return startAbility;
    }

    public void setStartAbility(Double startAbility) {
        this.startAbility = startAbility;
    }
}
