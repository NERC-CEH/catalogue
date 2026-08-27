package uk.ac.ceh.gateway.catalogue.researchActivity;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.List;

import tools.jackson.databind.json.JsonMapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
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

    @Test
    @DisplayName("getFunders preserves the order funding was entered in")
    void getFundersPreservesOrder() {
        // groupingBy returns a HashMap by default, so without an explicit
        // LinkedHashMap the rendered funder order is unrelated to the editor's.
        // given
        var researchActivity = new ResearchActivity();
        researchActivity.setFunding(List.of(
            funding("Zoological Society", "https://ror.org/zzz", "Z1"),
            funding("Alpha Trust", "https://ror.org/aaa", "A1"),
            funding("Middle Council", "https://ror.org/mmm", "M1"),
            funding("Alpha Trust", "https://ror.org/aaa", "A2")
        ));

        // when
        var funders = researchActivity.getFunders();

        // then
        assertThat(
            funders.stream().map(ResearchActivity.Funder::getOrganisationName).toList(),
            contains("Zoological Society", "Alpha Trust", "Middle Council")
        );
        assertThat(funders.get(1).getAwards().size(), equalTo(2));
    }

    @Test
    @DisplayName("getOrganisations de-duplicates and drops contributors without an identifier")
    void getOrganisationsDeduplicates() {
        // given
        var researchActivity = new ResearchActivity();
        researchActivity.setContributors(List.of(
            ResponsibleParty.builder()
                .organisationName("UK Centre for Ecology & Hydrology")
                .organisationIdentifier("https://ror.org/00pggkr55")
                .build(),
            ResponsibleParty.builder()
                .organisationName("UK Centre for Ecology & Hydrology")
                .organisationIdentifier("https://ror.org/00pggkr55")
                .build(),
            ResponsibleParty.builder()
                .organisationName("Unaffiliated")
                .build()
        ));

        // when
        var organisations = researchActivity.getOrganisations();

        // then
        assertThat(organisations.size(), equalTo(1));
        assertThat(organisations.getFirst().getOrganisationName(), equalTo("UK Centre for Ecology & Hydrology"));
    }

    @Test
    @DisplayName("derived funders and organisations are not serialised")
    void derivedFieldsAreNotSerialised() {
        // They are computed from funding/contributors, so persisting them would
        // create a second source of truth that goes stale on the next edit.
        // given
        var researchActivity = new ResearchActivity();
        researchActivity.setFunding(List.of(
            funding("Natural Environment Research Council", "https://ror.org/02b5d8509", "NE/J015644/1")
        ));
        researchActivity.setContributors(List.of(
            ResponsibleParty.builder()
                .organisationName("UK Centre for Ecology & Hydrology")
                .organisationIdentifier("https://ror.org/00pggkr55")
                .build()
        ));

        // when
        var json = JsonMapper.builder().build().writeValueAsString(researchActivity);

        // then
        assertThat(json, containsString("funding"));
        assertThat(json, not(containsString("\"funders\"")));
        assertThat(json, not(containsString("\"organisations\"")));
    }

    private Funding funding(String funderName, String funderIdentifier, String awardNumber) {
        return Funding.builder()
            .funderName(funderName)
            .funderIdentifier(funderIdentifier)
            .awardNumber(awardNumber)
            .build();
    }
}
