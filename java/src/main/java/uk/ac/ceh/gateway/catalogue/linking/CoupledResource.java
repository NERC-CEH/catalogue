package uk.ac.ceh.gateway.catalogue.linking;

import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class CoupledResource {
    private final String fileIdentifier, resourceIdentifier;
    
    @Builder
    private CoupledResource(String fileIdentifier, String resourceIdentifier) {
        this.fileIdentifier = nullToEmpty(fileIdentifier);
        this.resourceIdentifier = nullToEmpty(resourceIdentifier);
    }
}