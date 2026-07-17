package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.geometry.Geometry;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("MonitoringFacility")
class MonitoringFacilityTest {

    @Test
    @DisplayName("has no geometry and boundingBox")
    void getEmptyWKTs() {
        //given
        val facility = new MonitoringFacility();
        val expected = Collections.emptyList();


        //when
        val actual = facility.getWKTs();

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    @DisplayName("has geometry and no boundingBox")
    void getWKTs() {
        //given
        val facility = new MonitoringFacility();
        val geometry = Geometry
            .builder()
            .geometryString("{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-1.53,53.25]}}")
            .build();
        facility.setGeometry(geometry);

        //when
        val actual = facility.getWKTs();

        //then
        assertThat(actual.size(), equalTo(1));
    }

    @Test
    @DisplayName("populateFromJenaService")
    void populateFromJenaService() {
        //given
        val facility = new MonitoringFacility();
        String uri = "https://example.com/facility/test";
        facility.setUri(uri);
        facility.setLocationConfidential(false);
        val jenaService = org.mockito.Mockito.mock(JenaLookupService.class);

        when(jenaService.relationshipCombinedGeometriesWithOwner(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility", false))
            .thenReturn("combined-geometry-wkt");
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/isPartOf"))
            .thenReturn(List.of(Link.builder().href("https://example.com/network/1").build()));
        when(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/uses"))
            .thenReturn(List.of(Link.builder().href("https://example.com/activity/1").build()));
        when(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises"))
            .thenReturn(List.of(Link.builder().href("https://example.com/programme/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/replaces"))
            .thenReturn(List.of(Link.builder().href("https://example.com/old-facility/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/replaces"))
            .thenReturn(List.of(Link.builder().href("https://example.com/new-facility/1").build()));
        when(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility"))
            .thenReturn(List.of(Link.builder().href("https://example.com/child-facility/1").build()));
        when(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility"))
            .thenReturn(List.of(Link.builder().href("https://example.com/parent-facility/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"))
            .thenReturn(List.of(Link.builder().href("https://example.com/related/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/relation"))
            .thenReturn(List.of(Link.builder().href("https://example.com/related/2").build()));

        //when
        facility.populateFromJenaService(jenaService);

        //then
        assertThat(facility.getRelCombinedGeometry(), equalTo("combined-geometry-wkt"));
        assertThat(facility.getRelBelongsToNetwork().size(), equalTo(1));
        assertThat(facility.getRelUsedBy().size(), equalTo(1));
        assertThat(facility.getRelUtilisedBy().size(), equalTo(1));
        assertThat(facility.getRelSupersedes().size(), equalTo(1));
        assertThat(facility.getRelSupersededBy().size(), equalTo(1));
        assertThat(facility.getRelChildFacility().size(), equalTo(1));
        assertThat(facility.getRelParentFacility().size(), equalTo(1));
        assertThat(facility.getRelRelated().size(), equalTo(2));
    }
}
