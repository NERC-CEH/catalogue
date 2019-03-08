package uk.ac.ceh.gateway.catalogue.search;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.NonNull;

public class HardcodedFacetFactory implements FacetFactory {

    @Override
    public Facet newInstance(@NonNull String key) {        
        switch(key) {
                
            case "resourceType":
                return Facet.builder()
                    .fieldName("resourceType")
                    .displayName("Resource type")
                    .hierarchical(false)
                    .isAdmin(true)
                    .build();
                
            case "state":
            return Facet.builder()
                .fieldName("state")
                .displayName("Status")
                .hierarchical(false)
                .isAdmin(true)
                .build();
                    
            case "recordType":
                return Facet.builder()
                    .fieldName("recordType")
                    .displayName("Record type")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();
                
            case "licence":
                return Facet.builder()
                    .fieldName("licence")
                    .displayName("Licence")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();
                
            case "status":
                return Facet.builder()
                    .fieldName("resourceStatus")
                    .displayName("Availability")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();
                
            case "topic":        
                return Facet.builder()
                    .fieldName("topic")
                    .displayName("Topic")
                    .hierarchical(true)
                    .isAdmin(false)
                    .build();
                
            case "impCaMMPIssues":
                return Facet.builder()
                    .fieldName("impCaMMPIssues")
                    .displayName("CaMMP Issues")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();
                
            case "impDataType":
                return Facet.builder()
                    .fieldName("impDataType")
                    .displayName("Data Type")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();
                
            case "impScale":
                return Facet.builder()
                    .fieldName("impScale")
                    .displayName("Scale")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();
                
            case "impTopic":
                return Facet.builder()
                    .fieldName("impTopic")
                    .displayName("Topic")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();
                
            case "impWaterPollutant":
                return Facet.builder()
                    .fieldName("impWaterPollutant")
                    .displayName("Water Pollutant")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();
                
            case "inmsPollutant":
                return Facet.builder()
                    .fieldName("impWaterPollutant")
                    .displayName("Pollutant")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();
                
            case "inmsDemonstrationRegion":
                return Facet.builder()
                    .fieldName("inmsDemonstrationRegion")
                    .displayName("Demonstration Region")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();
                
            case "modelType":
                return Facet.builder()
                    .fieldName("modelType")
                    .displayName("Model Type")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();

            case "documentType":
                return Facet.builder()
                    .fieldName("documentType")
                    .displayName("Document Type")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();
            
            case "ncAssets":
                return Facet.builder()
                    .fieldName("ncAssets")
                    .displayName("Assets")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();

            case "ncCaseStudy":
                return Facet.builder()
                    .fieldName("ncCaseStudy")
                    .displayName("Case Study")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();

            case "ncDrivers":
                return Facet.builder()
                    .fieldName("ncDrivers")
                    .displayName("Drivers")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();

            case "ncEcosystemServices":
                return Facet.builder()
                    .fieldName("ncEcosystemServices")
                    .displayName("Ecosystem Services")
                    .hierarchical(false)
                    .isAdmin(false)
                    .build();

            case "ncGeographicalScale":
                return Facet.builder()
                    .fieldName("ncGeographicalScale")
                    .displayName("Geographical Scale")
                    .hierarchical(false)
                    .isAdmin(false)
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
