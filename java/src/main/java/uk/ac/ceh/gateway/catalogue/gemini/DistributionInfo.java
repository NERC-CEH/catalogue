package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Builder;
import lombok.Value;

@Value
public class DistributionInfo {
    private final String name, version;
    
    @Builder
    @JsonCreator
    private DistributionInfo(@JsonProperty("name") String name, @JsonProperty("version") String version) {
        this.name = nullToEmpty(name);
        this.version = nullToEmpty(version);
    } 
}