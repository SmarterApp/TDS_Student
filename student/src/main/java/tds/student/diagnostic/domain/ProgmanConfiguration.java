package tds.student.diagnostic.domain;

import java.util.List;

public class ProgmanConfiguration extends AbstractStatus {

    private String id;
    private String name;
    private String envName;

    private List<Setting> properties;

    public ProgmanConfiguration(String envName, String id, String name) {
        super(Rating.IDEAL);
        this.envName = envName;
        this.id = id;
        this.name = name;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Setting> getProperties() {
        return properties;
    }

    public void setProperties(List<Setting> properties) {
        this.properties = properties;
    }

}
