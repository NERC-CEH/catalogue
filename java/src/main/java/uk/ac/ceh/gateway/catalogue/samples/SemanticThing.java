package uk.ac.ceh.gateway.catalogue.samples;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
public class SemanticThing {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String predicate;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String object;

    @JsonAnyGetter
    public Map<String, String> asJson() {
        if (predicate == null) {
            return Collections.emptyMap();
        }

        return Collections.singletonMap(predicate, object);
    }

    @JsonAnySetter
    public void setProperty(String key, String value) {
        this.predicate = key;
        this.object = value;
    }
}