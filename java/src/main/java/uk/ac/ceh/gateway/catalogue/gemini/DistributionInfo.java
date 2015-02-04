package uk.ac.ceh.gateway.catalogue.gemini;

import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class DistributionInfo {
    private final String name, version;
    
    @Builder
    private DistributionInfo(String name, String version) {
        this.name = nullToEmpty(name);
        this.version = nullToEmpty(version);
    } 
}