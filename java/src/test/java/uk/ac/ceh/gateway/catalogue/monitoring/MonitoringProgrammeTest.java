package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.researchActivity.ResearchActivity;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("ResearchActivity")
class ResearchActivityTest {

    @Test
    @DisplayName("populateFromJenaService")
    void populateFromJenaService() {
        //given
        val researchActivity = new ResearchActivity();
        String uri = "https://example.com/programme/test";
        researchActivity.setUri(uri);
        val jenaService = org.mockito.Mockito.mock(JenaLookupService.class);

        when(jenaService.researchActivityCombinedGeometries("https://example.com/programme/test"))
            .thenReturn("combined-geometry-wkt");
        when(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises"))
            .thenReturn(List.of(Link.builder().href("https://example.com/network/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/replaces"))
            .thenReturn(List.of(Link.builder().href("https://example.com/old-programme/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/replaces"))
            .thenReturn(List.of(Link.builder().href("https://example.com/new-programme/1").build()));
        when(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/triggers"))
            .thenReturn(List.of(Link.builder().href("https://example.com/activity/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/hasPart"))
            .thenReturn(List.of(Link.builder().href("https://example.com/child-programme/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/hasPart"))
            .thenReturn(List.of(Link.builder().href("https://example.com/parent-programme/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"))
            .thenReturn(List.of(Link.builder().href("https://example.com/related/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/relation"))
            .thenReturn(List.of(Link.builder().href("https://example.com/related/2").build()));

        //when
        researchActivity.populateFromJenaService(jenaService);

        //then
        assertThat(researchActivity.getRelCombinedGeometry(), equalTo("combined-geometry-wkt"));
        assertThat(researchActivity.getRelUses().size(), equalTo(1));
        assertThat(researchActivity.getRelSupersedes().size(), equalTo(1));
        assertThat(researchActivity.getRelSupersededBy().size(), equalTo(1));
        assertThat(researchActivity.getRelActivities().size(), equalTo(1));
        //assertThat(researchActivity.getRelChildResearchActivity().size(), equalTo(1));
        //assertThat(researchActivity.getRelParentResearchActivity().size(), equalTo(1));
        assertThat(researchActivity.getRelRelated().size(), equalTo(2));
    }
}
