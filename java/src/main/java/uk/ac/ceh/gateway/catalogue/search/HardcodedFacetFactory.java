package uk.ac.ceh.gateway.catalogue.search;

import lombok.NonNull;
import uk.ac.ceh.gateway.catalogue.search.Facet;
import uk.ac.ceh.gateway.catalogue.search.FacetFactory;

public class HardcodedFacetFactory implements FacetFactory {

    @Override
    public Facet newInstance(@NonNull String key) {
        Facet instance;
        
        switch(key) {
            case "resourceType":
                instance = Facet.builder()
                    .fieldName("resourceType")
                    .displayName("Resource type")
                    .hierarchical(false)
                    .build();
                break;
                
            case "licence":
                instance = Facet.builder()
                    .fieldName("licence")
                    .displayName("Licence")
                    .hierarchical(false)
                    .build();
                break;
                
            case "topic":        
                instance = Facet.builder()
                    .fieldName("topic")
                    .displayName("Topic")
                    .hierarchical(true)
                    .build();
                break;
                
            case "impBroaderCatchmentIssues":
                instance = Facet.builder()
                    .fieldName("impBroaderCatchmentIssues")
                    .displayName("Broader Catchment Issues")
                    .hierarchical(false)
                    .build();
                break;
                
            case "impScale":
                instance = Facet.builder()
                    .fieldName("impScale")
                    .displayName("Scale")
                    .hierarchical(false)
                    .build();
                break;
                
            case "impWaterQuality":
                instance = Facet.builder()
                    .fieldName("impWaterQuality")
                    .displayName("Water Quality")
                    .hierarchical(false)
                    .build();
                break;
                
            default:
                instance = Facet.builder()
                    .fieldName("unknown")
                    .displayName("Unknown")
                    .hierarchical(false)
                    .build();
        }
        return instance;
    }
    
}
