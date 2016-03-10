package tds.student.diagnostic.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatabaseOperation extends AbstractStatus {

    private DatabaseOperationType type;
    private Long responseTime;

    public DatabaseOperation(Rating rating, DatabaseOperationType type, Long responseTime) {
        super(rating);
        this.type = type;
        this.responseTime = responseTime;
    }

    public DatabaseOperation(Rating rating, DatabaseOperationType type, Long responseTime, String error) {
        super(rating, error);
        this.type = type;
        this.responseTime = responseTime;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public DatabaseOperationType getType() {
        return type;
    }

    public void setType(DatabaseOperationType type) {
        this.type = type;
    }


}
