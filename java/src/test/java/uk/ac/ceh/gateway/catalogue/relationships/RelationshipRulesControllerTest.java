package uk.ac.ceh.gateway.catalogue.relationships;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ceh.gateway.catalogue.relationships.RelationshipRulesController.Targets;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ceh.gateway.catalogue.relationships.RelationshipRules.*;

/**
 * Uses the real codelist: the picker filters Solr's {@code resourceType}, which holds the
 * case-sensitive codelist label, so a wrong or missing label silently empties the search -
 * exactly how "Replaces" found nothing for any Gemini record (dri-one #439).
 */
@DisplayName("RelationshipRulesController")
class RelationshipRulesControllerTest {
    private final RelationshipRulesController controller =
        new RelationshipRulesController(new CodeLookupService("codelist.properties"));

    @Test
    @DisplayName("replaces from a dataset searches the Dataset label, not the dataset type key")
    void replacesUsesTheIndexedLabel() {
        assertThat(controller.rules("dataset").get(REPLACES))
            .isEqualTo(new Targets(List.of("Dataset"), List.of()));
    }

    @Test
    @DisplayName("produced at searches facilities and networks in eidc and ukceh")
    void utilises() {
        assertThat(controller.rules("dataset").get(UTILISES))
            .isEqualTo(new Targets(List.of("Monitoring facility", "Monitoring network"), List.of("eidc", "ukceh")));
    }

    @Test
    @DisplayName("is part of depends on the source: a collection for data, a network for a facility")
    void isPartOf() {
        assertThat(controller.rules("nonGeographicDataset").get(IS_PART_OF).resourceTypes())
            .isEqualTo(List.of("Aggregation"));
        assertThat(controller.rules("monitoringFacility").get(IS_PART_OF).resourceTypes())
            .isEqualTo(List.of("Monitoring network"));
    }

    @Test
    @DisplayName("open relationships are not listed")
    void openRelationshipsAreAbsent() {
        assertThat(controller.rules("dataset")).doesNotContainKey("http://purl.org/dc/terms/relation");
    }

    /**
     * Every type a rule can allow must have a label, or the picker would leave it out of its
     * search without any error.
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "dataset", "nonGeographicDataset", "aggregate", "service", "codeProject", "codeSnippet",
        "computationalNotebook", "infrastructureRecord", "researchActivity", "monitoringActivity",
        "monitoringFacility", "monitoringNetwork", "monitoringProgramme"
    })
    @DisplayName("every allowed target type has a resourceType label")
    void everyTargetTypeHasALabel(String sourceType) {
        var codes = new CodeLookupService("codelist.properties");
        predicates().forEach(predicate -> ruleFor(sourceType, predicate).ifPresent(rule ->
            rule.targetTypesFor(sourceType).forEach(type ->
                assertThat(codes.lookup("metadata.resourceType", type))
                    .as("label for %s (%s from %s)", type, predicate, sourceType)
                    .isNotBlank()
            )
        ));
    }
}
