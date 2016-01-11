package tds.student.performance.domain;

public class ItemForTesteeResponse {
    // add position as int
/*  A._fk_Item AS bankItemKey,         # string
    A.itemposition AS itemPosition,    # int
    I._efk_itembank AS bankKey,        # long
    I._efk_item AS itemKey,            # long
    A.irt_b AS irtb,                   # string need as float
    I.scorepoint AS scorePoint,        # int
    I.itemtype AS itemType,            # string
    A.isfieldtest AS isFieldTest,      # boolean
    A.isrequired AS isRequired,        # boolean
    A._fk_strand AS contentLevel,      # string
    F.formposition   AS formPosition,  # int
    I.answer AS answerKey              # string    */

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

    private Integer position=null;


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

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
