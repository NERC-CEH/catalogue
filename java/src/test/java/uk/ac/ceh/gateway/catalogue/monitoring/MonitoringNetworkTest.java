package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("MonitoringNetwork")
class MonitoringNetworkTest {

    @Test
    @DisplayName("populateFromJenaService")
    void populateFromJenaService() {
        //given
        val network = new MonitoringNetwork();
        String uri = "https://example.com/network/test";
        network.setUri(uri);
        val jenaService = org.mockito.Mockito.mock(JenaLookupService.class);

        when(jenaService.inverseRelationshipCombinedGeometries(uri, "http://purl.org/dc/terms/isPartOf"))
            .thenReturn("combined-geometry-wkt");
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/isPartOf"))
            .thenReturn(List.of(Link.builder().href("https://example.com/facility/1").build()));
        when(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/uses"))
            .thenReturn(List.of(Link.builder().href("https://example.com/activity/1").build()));
        when(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises"))
            .thenReturn(List.of(Link.builder().href("https://example.com/programme/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/replaces"))
            .thenReturn(List.of(Link.builder().href("https://example.com/old-network/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/replaces"))
            .thenReturn(List.of(Link.builder().href("https://example.com/new-network/1").build()));
        when(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildNetwork"))
            .thenReturn(List.of(Link.builder().href("https://example.com/child-network/1").build()));
        when(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildNetwork"))
            .thenReturn(List.of(Link.builder().href("https://example.com/parent-network/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"))
            .thenReturn(List.of(Link.builder().href("https://example.com/related/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/relation"))
            .thenReturn(List.of(Link.builder().href("https://example.com/related/2").build()));

        //when
        network.populateFromJenaService(jenaService);

        //then
        assertThat(network.getRelCombinedGeometry(), equalTo("combined-geometry-wkt"));
        assertThat(network.getRelFeatureList().size(), equalTo(1));
        assertThat(network.getRelUsedBy().size(), equalTo(1));
        assertThat(network.getRelUtilisedBy().size(), equalTo(1));
        assertThat(network.getRelSupersedes().size(), equalTo(1));
        assertThat(network.getRelSupersededBy().size(), equalTo(1));
        assertThat(network.getRelChildNetwork().size(), equalTo(1));
        assertThat(network.getRelParentNetwork().size(), equalTo(1));
        assertThat(network.getRelRelated().size(), equalTo(2));
    }
}
