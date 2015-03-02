package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonCreator
    private Service(
        @JsonProperty("type") String type,
        @JsonProperty("couplingType") String couplingType,
        @JsonProperty("versions") List<String> versions,
        @JsonProperty("coupledResources") List<CoupledResource> coupledResources, 
        @JsonProperty("containsOperations") List<OperationMetadata> containsOperations
    ) {
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
        @JsonCreator
        private CoupledResource(
            @JsonProperty("operationName") String operationName,
            @JsonProperty("identifier") String identifier,
            @JsonProperty("layerName") String layerName
        ) {
            this.operationName = Strings.nullToEmpty(operationName);
            this.identifier = Strings.nullToEmpty(identifier);
            this.layerName = Strings.nullToEmpty(layerName);
        } 
    }

    @Value
    public static class OperationMetadata {
        private final String operationName, platform, url;

        @Builder
        @JsonCreator
        private OperationMetadata(
            @JsonProperty("operationName") String operationName,
            @JsonProperty("platform") String platform,
            @JsonProperty("url") String url
        ) {
            this.operationName = Strings.nullToEmpty(operationName);
            this.platform = Strings.nullToEmpty(platform);
            this.url = Strings.nullToEmpty(url);
        } 
    }
}