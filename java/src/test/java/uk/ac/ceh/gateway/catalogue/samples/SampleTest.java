package uk.ac.ceh.gateway.catalogue.samples;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.List;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Sample")
class SampleTest {

    @Test
    @DisplayName("populateFromJenaService")
    void populateFromJenaService() {
        //given
        val sample = new Sample();
        String uri = "https://example.com/sample/test";
        sample.setUri(uri);
        val jenaService = org.mockito.Mockito.mock(JenaLookupService.class);

        when(jenaService.relationships(uri, "http://purl.org/cerif/frapo/hasOutput"))
            .thenReturn(List.of(Link.builder().href("https://example.com/output/1").build()));

        //when
        sample.populateFromJenaService(jenaService);

        //then
        assertThat(sample.getRelHasOutput().size(), equalTo(1));
    }
}
