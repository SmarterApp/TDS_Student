package tds.student.diagnostic.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Summary {

    private String unit;
    private String configuration = null;
    private String localSystem = null;
    private String providers = null;
    private String database = null;

    public Summary(Status status) {
        this.unit = status.getUnit();
        this.configuration = status.getConfiguration() != null ? status.getConfiguration().getStatusText() : null;
        this.localSystem = status.getLocalSystem() != null ? status.getLocalSystem().getStatusText() : null;
        this.providers = status.getProviders() != null ? status.getProviders().getStatusText() : null;
        this.database = status.getDatabase() != null ? status.getDatabase().getStatusText() : null;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getLocalSystem() {
        return localSystem;
    }

    public void setLocalSystem(String localSystem) {
        this.localSystem = localSystem;
    }

    public String getProviders() {
        return providers;
    }

    public void setProviders(String providers) {
        this.providers = providers;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
