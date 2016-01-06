package tds.student.performance.domain;

public class ConfigTestToolType {
    private String clientName;
    private String toolName;
    private String rtsFieldName;
    private String source;
    private String context;
    private String contextType;
    private String testMode;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getRtsFieldName() {
        return rtsFieldName;
    }

    public void setRtsFieldName(String rtsFieldName) {
        this.rtsFieldName = rtsFieldName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getContextType() {
        return contextType;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    public String getTestMode() {
        return testMode;
    }

    public void setTestMode(String testMode) {
        this.testMode = testMode;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
