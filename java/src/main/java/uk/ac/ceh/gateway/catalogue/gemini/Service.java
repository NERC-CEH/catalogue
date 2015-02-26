package uk.ac.ceh.gateway.catalogue.gemini;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
public class Service {
    private final String type, couplingType;
    private final List<String> versions;
    private final List<CoupledResource> coupledResources;
    private final List<OperationMetadata> containsOperations; 

    @Builder
    private Service(String type, String couplingType, List<String> versions, List<CoupledResource> coupledResources, List<OperationMetadata> containsOperations) {
        this.type = Strings.nullToEmpty(type);
        this.couplingType = Strings.nullToEmpty(couplingType);
        this.versions = (versions != null)? versions : Collections.EMPTY_LIST;
        this.coupledResources = (coupledResources != null)? coupledResources : Collections.EMPTY_LIST;
        this.containsOperations = (containsOperations != null)? containsOperations : Collections.EMPTY_LIST;
    }

    @Value
    public static class CoupledResource {
        private final String operationName, identifier, layerName;

        @Builder
        private CoupledResource(String operationName, String identifier, String layerName) {
            this.operationName = Strings.nullToEmpty(operationName);
            this.identifier = Strings.nullToEmpty(identifier);
            this.layerName = Strings.nullToEmpty(layerName);
        } 
    }

    @Value
    public static class OperationMetadata {
        private final String operationName, platform, url;

        @Builder
        private OperationMetadata(String operationName, String platform, String url) {
            this.operationName = Strings.nullToEmpty(operationName);
            this.platform = Strings.nullToEmpty(platform);
            this.url = Strings.nullToEmpty(url);
        } 
    }
}