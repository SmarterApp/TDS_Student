package tds.student.performance.exceptions;

public class ReturnErrorException extends Exception {


    private String status;
    private String reason;
    private String context;
    private String appKey;

    public ReturnErrorException(String message) {
        super(message);
    }

    public ReturnErrorException(String status, String reason, String context, String appKey) {
        super(String.format("Error status: %s, reason: %s, context: %s, appKey: %s", status, reason, context, appKey));

        this.status = status;
        this.reason = reason;
        this.context = context;
        this.appKey = appKey;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getContext() {
        return context;
    }

    public String getAppKey() {
        return appKey;
    }
}
