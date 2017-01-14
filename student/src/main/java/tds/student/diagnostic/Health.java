package tds.student.diagnostic;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.HashMap;
import java.util.Map;

@AutoValue
public abstract class Health {
    @JsonCreator
    public static Health create(@JsonProperty("status") String status) {
        return new AutoValue_Health(status, new HashMap<String, Object>());
    }

    public abstract String getStatus();

    @JsonAnyGetter
    public abstract Map<String, Object> getDetails();

    @JsonAnySetter
    public void add(final String key, final Object value) {
        getDetails().put(key, value);
    }
}