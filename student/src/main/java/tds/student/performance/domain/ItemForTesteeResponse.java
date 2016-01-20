package tds.student.performance.domain;

public class ItemForTesteeResponse {

    private String bankItemKey;
    private Integer itemPosition;
    private Long bankKey;
    private Long itemKey;
    private Float irtb;
    private String scorePoint;
    private String itemType;
    private Boolean isFieldTest;
    private Boolean isRequired;
    private String contentLevel;
    private Integer formPosition;
    private String answerKey;

    public ItemForTesteeResponse() {
    }

    public ItemForTesteeResponse(ItemForTesteeResponse in) {
        bankItemKey = in.getBankItemKey();
        itemPosition = in.getItemPosition();
        bankKey = in.getBankKey();
        itemKey = in.getItemKey();
        irtb = in.getIrtb();
        scorePoint = in.getScorePoint();
        itemType = in.getItemType();
        isFieldTest = in.getIsFieldTest();
        isRequired = in.getIsRequired();
        contentLevel = in.getContentLevel();
        formPosition = in.getFormPosition();
        answerKey = in.getAnswerKey();
    }


    public String getBankItemKey() {
        return bankItemKey;
    }

    public void setBankItemKey(String bankItemKey) {
        this.bankItemKey = bankItemKey;
    }

    public Integer getItemPosition() {
        return itemPosition;
    }

    public void setItemPosition(Integer itemPosition) {
        this.itemPosition = itemPosition;
    }

    public Long getBankKey() {
        return bankKey;
    }

    public void setBankKey(Long bankKey) {
        this.bankKey = bankKey;
    }

    public Long getItemKey() {
        return itemKey;
    }

    public void setItemKey(Long itemKey) {
        this.itemKey = itemKey;
    }

    public Float getIrtb() {
        return irtb;
    }

    public void setIrtb(Float irtb) {
        this.irtb = irtb;
    }

    public String getScorePoint() {
        return scorePoint;
    }

    public void setScorePoint(String scorePoint) {
        this.scorePoint = scorePoint;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public Boolean getIsFieldTest() {
        return isFieldTest;
    }

    public void setIsFieldTest(Boolean isFieldTest) {
        this.isFieldTest = isFieldTest;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public String getContentLevel() {
        return contentLevel;
    }

    public void setContentLevel(String contentLevel) {
        this.contentLevel = contentLevel;
    }

    public Integer getFormPosition() {
        return formPosition;
    }

    public void setFormPosition(Integer formPosition) {
        this.formPosition = formPosition;
    }

    public String getAnswerKey() {
        return answerKey;
    }

    public void setAnswerKey(String answerKey) {
        this.answerKey = answerKey;
    }

    @Override
    public String toString() {
        return "Item {" +
                "bankItemKey='" + bankItemKey + '\'' +
                ", itemPosition=" + itemPosition +
                ", bankKey=" + bankKey +
                ", itemKey=" + itemKey +
                ", irtb=" + irtb +
                ", scorePoint='" + scorePoint + '\'' +
                ", itemType='" + itemType + '\'' +
                ", isFieldTest=" + isFieldTest +
                ", isRequired=" + isRequired +
                ", contentLevel='" + contentLevel + '\'' +
                ", formPosition=" + formPosition +
                ", answerKey='" + answerKey + '\'' +
                '}';
    }
}
