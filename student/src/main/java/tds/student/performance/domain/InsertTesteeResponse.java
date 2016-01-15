package tds.student.performance.domain;


public class InsertTesteeResponse extends ItemForTesteeResponse {

    Integer position;

    public InsertTesteeResponse( ItemForTesteeResponse itemForTesteeResponse) {

        super(itemForTesteeResponse);
        position = null;

    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "InsertTesteeResponse{" +
                "position=" + position +
                "} " + super.toString();
    }
}

