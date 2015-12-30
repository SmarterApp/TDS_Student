package tds.student.performance.exceptions;

import java.util.UUID;


public class ReturnErrorException extends Exception {


    private String status;
    private String reason;
    private String context;
    private UUID appKey;

    public ReturnErrorException(String message) {
        super(message);
    }

    public ReturnErrorException(String status, String reason, String context, UUID appKey) {
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

    public UUID getAppKey() {
        return appKey;
    }
}
