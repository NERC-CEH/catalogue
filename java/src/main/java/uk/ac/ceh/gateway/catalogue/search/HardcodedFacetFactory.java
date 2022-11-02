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
        return switch (key) {
            case "assistResearchThemes" -> Facet.builder()
                .fieldName("assistResearchThemes")
                .displayName("Research Theme")
                .hierarchical(false)
                .build();
            case "assistTopics" -> Facet.builder()
                .fieldName("assistTopics")
                .displayName("Topic")
                .hierarchical(false)
                .build();
            case "condition" -> Facet.builder()
                .fieldName("condition")
                .displayName("Status")
                .hierarchical(false)
                .build();
            case "documentType" -> Facet.builder()
                .fieldName("documentType")
                .displayName("Document Type")
                .hierarchical(false)
                .build();
            case "elterDeimsSite" -> Facet.builder()
                .fieldName("elterDeimsSite")
                .displayName("Site")
                .hierarchical(false)
                .build();
            case "funder" -> Facet.builder()
                .fieldName("funder")
                .displayName("Funder")
                .hierarchical(false)
                .build();
            case "impCaMMPIssues" -> Facet.builder()
                .fieldName("impCaMMPIssues")
                .displayName("CaMMP Issues")
                .hierarchical(false)
                .build();
            case "impDataType" -> Facet.builder()
                .fieldName("impDataType")
                .displayName("Data Type")
                .hierarchical(false)
                .build();
            case "impScale" -> Facet.builder()
                .fieldName("impScale")
                .displayName("Scale")
                .hierarchical(false)
                .build();
            case "impTopic" -> Facet.builder()
                .fieldName("impTopic")
                .displayName("Topic")
                .hierarchical(false)
                .build();
            case "impWaterPollutant" -> Facet.builder()
                .fieldName("impWaterPollutant")
                .displayName("Water Pollutant")
                .hierarchical(false)
                .build();
            case "inmsPollutant" -> Facet.builder()
                .fieldName("impWaterPollutant")
                .displayName("Pollutant")
                .hierarchical(false)
                .build();
            case "inmsDemonstrationRegion" -> Facet.builder()
                .fieldName("inmsDemonstrationRegion")
                .displayName("Demonstration Region")
                .hierarchical(false)
                .build();
            case "inmsProject" -> Facet.builder()
                .fieldName("inmsProject")
                .displayName("Project")
                .hierarchical(false)
                .build();
            case "licence" -> Facet.builder()
                .fieldName("licence")
                .displayName("Licence")
                .hierarchical(false)
                .build();
            case "modelType" -> Facet.builder()
                .fieldName("modelType")
                .displayName("Model Type")
                .hierarchical(false)
                .build();
            case "ncAssets" -> Facet.builder()
                .fieldName("ncAssets")
                .displayName("Assets")
                .hierarchical(false)
                .build();
            case "ncCaseStudy" -> Facet.builder()
                .fieldName("ncCaseStudy")
                .displayName("Case Study")
                .hierarchical(false)
                .build();
            case "ncDrivers" -> Facet.builder()
                .fieldName("ncDrivers")
                .displayName("Drivers")
                .hierarchical(false)
                .build();
            case "ncEcosystemServices" -> Facet.builder()
                .fieldName("ncEcosystemServices")
                .displayName("Ecosystem Services")
                .hierarchical(false)
                .build();
            case "ncGeographicalScale" -> Facet.builder()
                .fieldName("ncGeographicalScale")
                .displayName("Geographical Scale")
                .hierarchical(false)
                .build();
            case "recordType" -> Facet.builder()
                .fieldName("recordType")
                .displayName("Record type")
                .hierarchical(false)
                .build();
            case "dataLevel" -> Facet.builder()
                .fieldName("dataLevel")
                .displayName("Data level")
                .hierarchical(false)
                .build();
           case "resourceType" -> Facet.builder()
                .fieldName("resourceType")
                .displayName("Resource type")
                .hierarchical(false)
                .build();
            case "rightsHolder" -> Facet.builder()
                .fieldName("rightsHolder")
                .displayName("Rights holder")
                .hierarchical(false)
                .build();
            case "infrastructureClass" -> Facet.builder()
                .fieldName("infrastructureClass")
                .displayName("Infrastructure class")
                .hierarchical(false)
                .build();
            case "infrastructureCategory" -> Facet.builder()
                .fieldName("infrastructureCategory")
                .displayName("Category")
                .hierarchical(false)
                .build();
            case "infrastructureChallenge" -> Facet.builder()
                .fieldName("infrastructureChallenge")
                .displayName("Goal/Challenge")
                .hierarchical(false)
                .build();
            case "infrastructureScale" -> Facet.builder()
                .fieldName("infrastructureScale")
                .displayName("Scale")
                .hierarchical(false)
                .build();
            case "saPhysicalState" -> Facet.builder()
                .fieldName("saPhysicalState")
                .displayName("Physical State")
                .hierarchical(false)
                .build();
            case "saSpecimenType" -> Facet.builder()
                .fieldName("saSpecimenType")
                .displayName("Specimen Type")
                .hierarchical(false)
                .build();
            case "saTaxon" -> Facet.builder()
                .fieldName("saTaxon")
                .displayName("Taxa")
                .hierarchical(false)
                .build();
            case "saTissue" -> Facet.builder()
                .fieldName("saTissue")
                .displayName("Tissue")
                .hierarchical(false)
                .build();
            case "state" -> Facet.builder()
                .fieldName("state")
                .displayName("Status")
                .hierarchical(false)
                .build();
            case "status" -> Facet.builder()
                .fieldName("resourceStatus")
                .displayName("Availability")
                .hierarchical(false)
                .build();
            case "topic" -> Facet.builder()
                .fieldName("topic")
                .displayName("Topic")
                .hierarchical(true)
                .build();
            case "ukscapeResearchProject" -> Facet.builder()
                .fieldName("ukscapeResearchProject")
                .displayName("Research Project")
                .hierarchical(false)
                .build();
            case "ukscapeResearchTheme" -> Facet.builder()
                .fieldName("ukscapeResearchTheme")
                .displayName("Research Theme")
                .hierarchical(false)
                .build();
            case "ukscapeScienceChallenge" -> Facet.builder()
                .fieldName("ukscapeScienceChallenge")
                .displayName("Science Challenge")
                .hierarchical(false)
                .build();
            case "ukscapeService" -> Facet.builder()
                .fieldName("ukscapeService")
                .displayName("Service")
                .hierarchical(false)
                .build();
            default -> null;
        };
    }

    @Override
    public List<Facet> newInstances(List<String> facetKeys) {
        return facetKeys
            .stream()
            .map(this::newInstance)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

}
