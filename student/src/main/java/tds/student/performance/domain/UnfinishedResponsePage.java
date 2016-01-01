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
