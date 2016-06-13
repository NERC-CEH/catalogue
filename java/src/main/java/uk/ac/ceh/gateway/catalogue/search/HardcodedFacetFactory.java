package uk.ac.ceh.gateway.catalogue.search;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;

public class HardcodedFacetFactory implements FacetFactory {

    @Override
    public Facet newInstance(@NonNull String key) {        
        switch(key) {
            
            case "catalogue":
                return Facet.builder()
                    .fieldName("catalogue")
                    .displayName("Catalogue")
                    .hierarchical(false)
                    .build();
                
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
                
            case "impBroaderCatchmentIssues":
                return Facet.builder()
                    .fieldName("impBroaderCatchmentIssues")
                    .displayName("Broader Catchment Issues")
                    .hierarchical(false)
                    .build();
                
            case "impScale":
                return Facet.builder()
                    .fieldName("impScale")
                    .displayName("Scale")
                    .hierarchical(false)
                    .build();
                
            case "impWaterQuality":
                return Facet.builder()
                    .fieldName("impWaterQuality")
                    .displayName("Water Quality")
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
