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

@DisplayName("MonitoringProgramme")
class MonitoringProgrammeTest {

    @Test
    @DisplayName("populateFromJenaService")
    void populateFromJenaService() {
        //given
        val programme = new MonitoringProgramme();
        String uri = "https://example.com/programme/test";
        programme.setUri(uri);
        val jenaService = org.mockito.Mockito.mock(JenaLookupService.class);

        when(jenaService.programmeCombinedGeometries("https://example.com/programme/test"))
            .thenReturn("combined-geometry-wkt");
        when(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises"))
            .thenReturn(List.of(Link.builder().href("https://example.com/network/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/replaces"))
            .thenReturn(List.of(Link.builder().href("https://example.com/old-programme/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/replaces"))
            .thenReturn(List.of(Link.builder().href("https://example.com/new-programme/1").build()));
        when(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/triggers"))
            .thenReturn(List.of(Link.builder().href("https://example.com/activity/1").build()));
        when(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildProgramme"))
            .thenReturn(List.of(Link.builder().href("https://example.com/child-programme/1").build()));
        when(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildProgramme"))
            .thenReturn(List.of(Link.builder().href("https://example.com/parent-programme/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"))
            .thenReturn(List.of(Link.builder().href("https://example.com/related/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/relation"))
            .thenReturn(List.of(Link.builder().href("https://example.com/related/2").build()));

        //when
        programme.populateFromJenaService(jenaService);

        //then
        assertThat(programme.getRelCombinedGeometry(), equalTo("combined-geometry-wkt"));
        assertThat(programme.getRelUses().size(), equalTo(1));
        assertThat(programme.getRelSupersedes().size(), equalTo(1));
        assertThat(programme.getRelSupersededBy().size(), equalTo(1));
        assertThat(programme.getRelActivities().size(), equalTo(1));
        assertThat(programme.getRelChildProgramme().size(), equalTo(1));
        assertThat(programme.getRelParentProgramme().size(), equalTo(1));
        assertThat(programme.getRelRelated().size(), equalTo(2));
    }
}
