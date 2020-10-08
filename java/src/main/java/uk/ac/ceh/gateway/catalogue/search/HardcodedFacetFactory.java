package uk.ac.ceh.gateway.catalogue.search;

import lombok.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
                
            case "state":
                return Facet.builder()
                    .fieldName("state")
                    .displayName("Status")
                    .hierarchical(false)
                    .build();
                    
            case "recordType":
                return Facet.builder()
                    .fieldName("recordType")
                    .displayName("Record type")
                    .hierarchical(false)
                    .build();
            
            case "rightsHolder":
                return Facet.builder()
                    .fieldName("rightsHolder")
                    .displayName("Rights holder")
                    .hierarchical(false)
                    .build();

            case "elterDeimsSite":
                return Facet.builder()
                    .fieldName("elterDeimsSite")
                    .displayName("Site")
                    .hierarchical(false)
                    .build();
            
            case "funder":
                return Facet.builder()
                    .fieldName("funder")
                    .displayName("Funder")
                    .hierarchical(false)
                    .build();
                 
            case "licence":
                return Facet.builder()
                    .fieldName("licence")
                    .displayName("Licence")
                    .hierarchical(false)
                    .build();
                
            case "status":
                return Facet.builder()
                    .fieldName("resourceStatus")
                    .displayName("Availability")
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

            case "inmsProject":
                return Facet.builder()
                    .fieldName("inmsProject")
                    .displayName("Project")
                    .hierarchical(false)
                    .build();
                
            case "modelType":
                return Facet.builder()
                    .fieldName("modelType")
                    .displayName("Model Type")
                    .hierarchical(false)
                    .build();

            case "documentType":
                return Facet.builder()
                    .fieldName("documentType")
                    .displayName("Document Type")
                    .hierarchical(false)
                    .build();

            case "condition":
                return Facet.builder()
                    .fieldName("condition")
                    .displayName("Status")
                    .hierarchical(false)
                    .build();

            case "ncAssets":
                return Facet.builder()
                    .fieldName("ncAssets")
                    .displayName("Assets")
                    .hierarchical(false)
                    .build();

            case "ncCaseStudy":
                return Facet.builder()
                    .fieldName("ncCaseStudy")
                    .displayName("Case Study")
                    .hierarchical(false)          
                    .build();

            case "ncDrivers":
                return Facet.builder()
                    .fieldName("ncDrivers")
                    .displayName("Drivers")
                    .hierarchical(false)
                    .build();

            case "ncEcosystemServices":
                return Facet.builder()
                    .fieldName("ncEcosystemServices")
                    .displayName("Ecosystem Services")
                    .hierarchical(false)
                    .build();

            case "ncGeographicalScale":
                return Facet.builder()
                    .fieldName("ncGeographicalScale")
                    .displayName("Geographical Scale")
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
