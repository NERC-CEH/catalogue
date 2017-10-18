package uk.ac.ceh.gateway.catalogue.search;

import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class HardcodedFacetFactory implements FacetFactory {

    @Override
    public Facet newInstance(@NonNull String key) {        
        switch(key) {
                
            case "resourceType":
                return Facet.builder()
                    .fieldName("resourceType")
                    .displayName("Resource type")
                    .hierarchical(false)
                    .build();
                
            case "licence":
                return Facet.builder()
                    .fieldName("licence")
                    .displayName("Licence")
                    .hierarchical(false)
                    .build();
                
            case "topic":        
                return Facet.builder()
                    .fieldName("topic")
                    .displayName("Topic")
                    .hierarchical(true)
                    .build();
                
            case "impCaMMPIssues":
                return Facet.builder()
                    .fieldName("impCaMMPIssues")
                    .displayName("CaMMP Issues")
                    .hierarchical(false)
                    .build();
                
            case "impDataType":
                return Facet.builder()
                    .fieldName("impDataType")
                    .displayName("Data Type")
                    .hierarchical(false)
                    .build();
                
            case "impScale":
                return Facet.builder()
                    .fieldName("impScale")
                    .displayName("Scale")
                    .hierarchical(false)
                    .build();
                
            case "impTopic":
                return Facet.builder()
                    .fieldName("impTopic")
                    .displayName("Topic")
                    .hierarchical(false)
                    .build();
                
            case "impWaterPollutant":
                return Facet.builder()
                    .fieldName("impWaterPollutant")
                    .displayName("Water Pollutant")
                    .hierarchical(false)
                    .build();
                
            case "inmsPollutant":
                return Facet.builder()
                    .fieldName("impWaterPollutant")
                    .displayName("Pollutant")
                    .hierarchical(false)
                    .build();
                
            case "inmsDemonstrationRegion":
                return Facet.builder()
                    .fieldName("inmsDemonstrationRegion")
                    .displayName("Demonstration Region")
                    .hierarchical(false)
                    .build();
                
            case "modelType":
                return Facet.builder()
                    .fieldName("modelType")
                    .displayName("Model Type")
                    .hierarchical(false)
                    .build();
        }
        return null;
    }

    @Override
    public List<Facet> newInstances(List<String> facetKeys) {
        return facetKeys
            .stream()
            .map(f -> newInstance(f))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
}
