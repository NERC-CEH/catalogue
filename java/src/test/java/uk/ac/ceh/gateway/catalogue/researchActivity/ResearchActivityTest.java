package uk.ac.ceh.gateway.catalogue.researchActivity;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.model.Link;
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
        String uri = "https://example.com/researchActivity/test";
        researchActivity.setUri(uri);
        val jenaService = org.mockito.Mockito.mock(JenaLookupService.class);

        when(jenaService.relationships(uri, "http://purl.org/cerif/frapo/hasOutput"))
            .thenReturn(List.of(Link.builder().href("https://example.com/output/1").build()));

        //when
        researchActivity.populateFromJenaService(jenaService);

        //then
        assertThat(researchActivity.getRelHasOutput().size(), equalTo(1));
    }

    @Test
    @DisplayName("getFunders groups funding by organisation")
    void getFundersGroupsFunding() {
        // given
        var researchActivity = new ResearchActivity();

        researchActivity.setFunding(List.of(
            Funding.builder()
                .funderName("UKRI")
                .funderIdentifier("https://ror.org/ukri")
                .awardNumber("A001")
                .awardTitle("Project A")
                .awardURI("https://award/A001")
                .build(),
            Funding.builder()
                .funderName("UKRI")
                .funderIdentifier("https://ror.org/ukri")
                .awardNumber("A002")
                .awardTitle("Project B")
                .awardURI("https://award/A002")
                .build()
        ));

        // when
        var funders = researchActivity.getFunders();

        // then
        assertThat(funders.size(), equalTo(1));

        var funder = funders.getFirst();
        assertThat(funder.getOrganisationName(), equalTo("UKRI"));
        assertThat(funder.getOrganisationIdentifier(), equalTo("https://ror.org/ukri"));
        assertThat(funder.getAwards().size(), equalTo(2));
    }    
}
