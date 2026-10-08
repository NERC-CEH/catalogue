package uk.ac.ceh.gateway.catalogue.relationships;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ceh.gateway.catalogue.relationships.RelationshipRules.*;

@DisplayName("RelationshipRules")
class RelationshipRulesTest {

    @Test
    @DisplayName("a dataset is produced at a facility or network, searched for in eidc and ukceh")
    void utilises() {
        var rule = ruleFor("dataset", UTILISES).orElseThrow();

        assertThat(rule.targetTypesFor("dataset")).isEqualTo(Set.of("monitoringFacility", "monitoringNetwork"));
        assertThat(rule.catalogues()).isEqualTo(List.of("eidc", "ukceh"));
    }

    @Test
    @DisplayName("a programme's facility or network is searched for in eidc and ukceh too")
    void programmeUtilises() {
        assertThat(ruleFor("monitoringProgramme", UTILISES).orElseThrow().catalogues())
            .isEqualTo(List.of("eidc", "ukceh"));
    }

    @Test
    @DisplayName("replaces and supersedes point at the same type as the source")
    void sameType() {
        assertThat(ruleFor("service", REPLACES).orElseThrow().allows("service", "service")).isTrue();
        assertThat(ruleFor("service", REPLACES).orElseThrow().allows("service", "dataset")).isFalse();
        assertThat(ruleFor("codeProject", SUPERSEDES).orElseThrow().allows("codeProject", "codeProject")).isTrue();
        assertThat(ruleFor("codeProject", SUPERSEDES).orElseThrow().allows("codeProject", "codeSnippet")).isFalse();
    }

    @Test
    @DisplayName("a same-type rule allows nothing for a source with no type")
    void sameTypeWithoutSourceType() {
        assertThat(ruleFor(null, REPLACES).orElseThrow().allows(null, "dataset")).isFalse();
        assertThat(ruleFor("", REPLACES).orElseThrow().allows("", "")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"dataset", "nonGeographicDataset", "service", "aggregate", "signpost"})
    @DisplayName("every data resource type is part of a data collection")
    void dataResourceIsPartOf(String sourceType) {
        assertThat(ruleFor(sourceType, IS_PART_OF).orElseThrow().targetTypesFor(sourceType))
            .isEqualTo(Set.of("aggregate"));
    }

    @Test
    @DisplayName("a facility is part of a network")
    void facilityIsPartOf() {
        assertThat(ruleFor("monitoringFacility", IS_PART_OF).orElseThrow().targetTypesFor("monitoringFacility"))
            .isEqualTo(Set.of("monitoringNetwork"));
    }

    @Test
    @DisplayName("an activity uses facilities and networks, and a programme triggers activities")
    void monitoring() {
        assertThat(ruleFor("monitoringActivity", USES).orElseThrow().targetTypesFor("monitoringActivity"))
            .isEqualTo(Set.of("monitoringFacility", "monitoringNetwork"));
        assertThat(ruleFor("monitoringProgramme", TRIGGERS).orElseThrow().targetTypesFor("monitoringProgramme"))
            .isEqualTo(Set.of("monitoringActivity"));
    }

    @Test
    @DisplayName("services require, and code and infrastructure produce, data resources")
    void dataResources() {
        assertThat(ruleFor("service", REQUIRES).orElseThrow().allows("service", "dataset")).isTrue();
        assertThat(ruleFor("service", REQUIRES).orElseThrow().allows("service", "monitoringFacility")).isFalse();
        assertThat(ruleFor("codeProject", SOURCE).orElseThrow().allows("codeProject", "nonGeographicDataset")).isTrue();
        assertThat(ruleFor("infrastructureRecord", SOURCE).orElseThrow().allows("infrastructureRecord", "codeProject")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://purl.org/dc/terms/relation",
        "http://onto.nerc.ac.uk/CEHMD/rels/related",
        "http://onto.nerc.ac.uk/CEHMD/rels/cites",
        "http://onto.nerc.ac.uk/CEHMD/rels/uses"
    })
    @DisplayName("deliberately open relationships have no rule")
    void open(String predicate) {
        assertThat(ruleFor("dataset", predicate)).isEmpty();
        assertThat(ruleFor("codeProject", predicate)).isEmpty();
    }

    @Test
    @DisplayName("a research activity's outputs are models, data, map services and software")
    void hasOutput() {
        var rule = ruleFor("researchActivity", HAS_OUTPUT).orElseThrow();
        for (String output : List.of("model", "nercModel", "dataset", "nonGeographicDataset", "service", "software")) {
            assertThat(rule.allows("researchActivity", output)).as(output).isTrue();
        }
        assertThat(rule.allows("researchActivity", "monitoringFacility")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {HAS_CHILD_FACILITY, HAS_CHILD_NETWORK, HAS_CHILD_PROGRAMME})
    @DisplayName("a child facility, network or programme is of the same type as its parent")
    void hasChild(String predicate) {
        var rule = ruleFor("monitoringNetwork", predicate).orElseThrow();
        assertThat(rule.allows("monitoringNetwork", "monitoringNetwork")).isTrue();
        assertThat(rule.allows("monitoringNetwork", "monitoringFacility")).isFalse();
    }

    @Test
    @DisplayName("a relationship with no predicate has no rule")
    void noPredicate() {
        assertThat(ruleFor("dataset", null)).isEmpty();
    }
}
