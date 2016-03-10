package tds.student.diagnostic.domain;

import java.util.List;

public class Configuration extends AbstractStatus {

    private List<Setting> environmentSettings;
    private List<ProgmanConfiguration> progmanConfigurations;

    public Configuration(Rating rating, List<Setting> environmentSettings) {
        super(rating);
        this.environmentSettings = environmentSettings;
    }

    public List<Setting> getEnvironmentSettings() {
        return environmentSettings;
    }

    public void setEnvironmentSettings(List<Setting> environmentSettings) {
        this.environmentSettings = environmentSettings;
    }

    public List<ProgmanConfiguration> getProgmanConfigurations() {
        return progmanConfigurations;
    }

    public void setProgmanConfigurations(List<ProgmanConfiguration> progmanConfigurations) {
        this.progmanConfigurations = progmanConfigurations;
    }

}
