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

@DisplayName("MonitoringActivity")
class MonitoringActivityTest {

    @Test
    @DisplayName("populateFromJenaService")
    void populateFromJenaService() {
        //given
        val activity = new MonitoringActivity();
        String uri = "https://example.com/activity/test";
        activity.setUri(uri);
        val jenaService = org.mockito.Mockito.mock(JenaLookupService.class);

        when(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/uses"))
            .thenReturn(List.of(Link.builder().href("https://example.com/network/1").build()));
        when(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/triggers"))
            .thenReturn(List.of(Link.builder().href("https://example.com/programme/1").build()));

        //when
        activity.populateFromJenaService(jenaService);

        //then
        assertThat(activity.getRelUseNetworkOrFacility().size(), equalTo(1));
        assertThat(activity.getRelSetupForProgramme().size(), equalTo(1));
    }
}
