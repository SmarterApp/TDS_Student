package tds.student.performance.domain;


public class StudentLoginField {

    private String tdsId;
    private String rtsName;
    private String fieldType;
    private String atLogin;
    private String label;
    private String sortOrder;

    public StudentLoginField() {
    }

    public String getTdsId() {
        return tdsId;
    }

    public void setTdsId(String tdsId) {
        this.tdsId = tdsId;
    }

    public String getRtsName() {
        return rtsName;
    }

    public void setRtsName(String rtsName) {
        this.rtsName = rtsName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getAtLogin() {
        return atLogin;
    }

    public void setAtLogin(String atLogin) {
        this.atLogin = atLogin;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(this);
    }

}
