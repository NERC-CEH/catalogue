package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;

@Slf4j
public class GeminiDocumentTest {
    private final String id = "c43818fc-61fb-455b-9714-072355597229";
    private final String rel1 = "https://example.com/rel/1";
    private final String doc1 = "https://example.com/doc/1";
    private final String doc2 = "https://example.com/doc/2";
    private final String doc3 = "https://example.com/doc/3";

    @Test
    void deserializesLegacyResponsiblePartiesIntoTypedFields() throws Exception {
        //given
        val json = """
            {
              "responsibleParties": [
                {"role": "author", "displayName": "Author, 0."},
                {"role": "publisher", "organisationName": "Publisher Org"},
                {"role": "custodian", "organisationName": "Custodian Org"},
                {"role": "pointOfContact", "displayName": "POC, 0."},
                {"role": "rightsHolder", "organisationName": "Rights Org"},
                {"role": "depositor", "displayName": "Depositor, 0."}
              ]
            }
            """;

        //when
        val gemini = new ObjectMapper().readValue(json, GeminiDocument.class);

        //then
        assertThat(gemini.getAuthors().size(), equalTo(1));
        assertThat(gemini.getPublishers().size(), equalTo(1));
        assertThat(gemini.getCustodians().size(), equalTo(1));
        assertThat(gemini.getContactPoints().size(), equalTo(1));
        assertThat(gemini.getRightsHolders().size(), equalTo(1));
        assertThat(gemini.getDepositors().size(), equalTo(1));
    }

    @Test
    void getDistributions() {
        //given
        val gemini = new GeminiDocument();
        gemini.setOnlineResources(List.of(
            OnlineResource.builder().function("download").build(),
            OnlineResource.builder().function("order").build(),
            OnlineResource.builder().function("fileAccess").build(),
            OnlineResource.builder().function("somethingElse").build()
        ));

        //when
        val actual = gemini.getDistributions();

        //then
        assertThat(actual.size(), equalTo(3));
    }

    @Test
    void getAllKeywordsWhenEmpty() {
        //given
        val document = new GeminiDocument();

        //when
        val actual = document.getAllKeywords();

        //then
        assertThat(actual, is(empty()));
    }

    @Test
    void getAllKeywordsWithValuesFromMultipleKeywords() {
        //given
        val document = new GeminiDocument();
        document.setKeywordsOther(List.of(
            Keyword.builder().value("other 1").build(),
            Keyword.builder().value("other 2").build()
        ));
        document.setKeywordsDiscipline(List.of(
            Keyword.builder().value("discipline 1").build(),
            Keyword.builder().value("discipline 2").build()
        ));
        document.setKeywordsPlace(List.of(
            Keyword.builder().value("place 1").build(),
            Keyword.builder().value("place 2").build()
        ));

        //when
        val actual = document.getAllKeywords();

        //then
        assertThat(actual.size(), equalTo(6));
    }

    @Test
    void relationshipsFromRelatedRecordsNonePopulated() {
        // given
        val expected = Sets.newHashSet();
        val document = new GeminiDocument();

        // when
        val actual = document.getRelationships();

        // then
        assertThat(actual, equalTo(expected));
    }

    @Test
    void relationshipsFromBoth() {
        // given
        val expected = Sets.newHashSet(
            new Relationship(rel1, doc1),
            new Relationship(rel1, doc2),
            new Relationship(rel1, doc3)
        );
        val document = new GeminiDocument();
        document.setRelationships(Set.of(
            new Relationship(rel1, doc1),
            new Relationship(rel1, doc2),
            new Relationship(rel1, doc3)
        ));

        // when
        val actual = document.getRelationships();

        // then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void noMapViewerUrlIfGetCapabilitiesOnlineResourceDoesNotExists() {
        //Given
        OnlineResource wmsResource = OnlineResource.builder()
                .url("https://www.google.com")
                .build();
        GeminiDocument document = new GeminiDocument();
        document.setOnlineResources(List.of(wmsResource));

        //When
        val url = document.getMapViewerUrl();

        //Then
        assertThat(url, is(nullValue()));
    }

    @Test
    public void getLinkToMapViewer() {
        //Given
        val document = new GeminiDocument();
        document.setOnlineResources(List.of(
            OnlineResource.builder()
                .url("https://example.com/maps/" + id + "?request=getCapabilities&service=WMS")
                .build()
        ));

        //When
        String url = document.getMapViewerUrl();

        //Then
        assertThat(url, equalTo("/maps#layers/" + id));
    }

    @Test
    public void checkThatMapViewerURLIsNullIfNotMapViewable() {
        //Given
        GeminiDocument document = new GeminiDocument();

        //When
        String url = document.getMapViewerUrl();

        //Then
        assertThat(url, is(nullValue()));
    }

    @Test
    public void checkThatMetadataDateTimeIsEmptyStringIfNoMetadataDate() {
        //Given
        GeminiDocument document = new GeminiDocument();

        //When
        String actual = document.getMetadataDateTime();

        //Then
        assertThat("MetadataDateTime should be empty string", actual, equalTo(""));

    }

    @Test
    public void getIncomingCitationCount() {
        // Given
        GeminiDocument document = new GeminiDocument();
        Supplemental citation1 = Supplemental.builder().description("foo").build();
        Supplemental citation2 = Supplemental.builder().description("bar").build();
        List<Supplemental> citations = new ArrayList<>();
        citations.add(citation1);
        citations.add(citation2);
        document.setIncomingCitations(citations);
        long expected = 2;

        // When
        long output = document.getIncomingCitationCount();

        // Then
        assertThat(output, is(expected));
    }

    @Test
    public void unknownAvailability() {
        //given
        val document = new GeminiDocument();

        //when
        val actual = document.getAvailability();

        //then
        assertThat(actual, equalTo("Unknown"));
    }

    @Test
    public void accessLimitationAndAvailability() {
        //given
        val document = new GeminiDocument();
        document.setAccessLimitation(AccessLimitation.builder().build());

        //when
        val actual = document.getAvailability();

        //then
        assertThat(actual, equalTo("Unknown"));
    }

    private List<ResponsibleParty> getResponsibleParties(String role) {
        ResponsibleParty responsibleParty = ResponsibleParty.builder()
            .familyName("Smith")
            .givenName("Bob")
            .role(role)
            .build();
        return Arrays.asList(new ResponsibleParty[] {responsibleParty});
    }

    @Test void mutablePrePopulatedListProperties(){
        // given
        GeminiDocument document = new GeminiDocument();

        // one coupled service
        Service.CoupledResource coupledResource = Service.CoupledResource.builder().operationName("COM").layerName("foo").identifier("https://bar.com").build();
        document.setService(Service.builder().coupledResources(List.of(coupledResource)).build());

        // two topics
        document.setKeywordsTheme(List.of(
            Keyword.builder().value("a").URI("http://onto.nerc.ac.uk/CEHMD/").build(),
            Keyword.builder().value("b").URI("http://onto.nerc.ac.uk/CEHMD/").build()
        ));

        // three authors
        document.setAuthors(
            Arrays.asList(
                ResponsibleParty.builder().build(),
                ResponsibleParty.builder().build(),
                ResponsibleParty.builder().build()
            )
        );

        // when
        List<String> actualCoupledResources = document.getCoupledResources();
        List<String> actualTopics = document.getTopics();
        List<ResponsibleParty> actualAuthors = document.getAuthors();
        actualCoupledResources.add("foo");
        actualTopics.add("foo");
        actualAuthors.add(ResponsibleParty.builder().build());

        // then
        assertThat(actualCoupledResources.size(), equalTo(2));
        assertThat(actualTopics.size(), equalTo(3));
        assertThat(actualAuthors.size(), equalTo(4));
    }

    @Test void mutableEmptyListProperties(){
        // given
        GeminiDocument document = new GeminiDocument();

        // when
        List<String> actualCoupledResources = document.getCoupledResources();
        List<String> actualTopics = document.getTopics();
        List<ResponsibleParty> actualAuthors = document.getAuthors();
        List<Keyword> actualKeywords = document.getAllKeywords();
        List<OnlineResource> actualOnlineResources = document.getOnlineResources();
        actualCoupledResources.add("foo");
        actualTopics.add("foo");
        actualAuthors.add(ResponsibleParty.builder().build());
        actualKeywords.add(Keyword.builder().value("foo").URI("https://foo.com").build());
        actualOnlineResources.add(OnlineResource.builder().url("foo").build());


        // then
        assertThat(actualCoupledResources.size(), equalTo(1));
        assertThat(actualTopics.size(), equalTo(1));
        assertThat(actualAuthors.size(), equalTo(1));
        assertThat(actualKeywords.size(), equalTo(1));
        assertThat(actualOnlineResources.size(), equalTo(1));
    }

    @Test
    void populateFromJenaService() {
        //given
        val document = new GeminiDocument();
        String uri = "https://example.com/doc/test";
        document.setUri(uri);
        val jenaService = org.mockito.Mockito.mock(JenaLookupService.class);

        when(jenaService.relationships(uri, "http://purl.org/dc/terms/relation"))
            .thenReturn(List.of(Link.builder().href("https://example.com/rel/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"))
            .thenReturn(List.of(Link.builder().href("https://example.com/rel/2").build()));
        when(jenaService.allRelatedRecords(uri))
            .thenReturn(List.of(Link.builder().href("https://example.com/all/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/requires"))
            .thenReturn(List.of(Link.builder().href("https://example.com/requires/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/isPartOf"))
            .thenReturn(List.of(Link.builder().href("https://example.com/partof/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/requires"))
            .thenReturn(List.of(Link.builder().href("https://example.com/requiredby/1").build()));
        when(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/isPartOf"))
            .thenReturn(List.of(Link.builder().href("https://example.com/haspart/1").build()));
        when(jenaService.replaces(uri))
            .thenReturn(List.of(Link.builder().href("https://example.com/replaces/1").build()));
        when(jenaService.relationships(uri, "http://purl.org/dc/terms/source"))
            .thenReturn(List.of(Link.builder().href("https://example.com/source/1").build()));

        //when
        document.populateFromJenaService(jenaService);

        //then
        assertThat(document.getRelRelation().size(), equalTo(2));
        assertThat(document.getRelAll().size(), equalTo(1));
        assertThat(document.getRelRequires().size(), equalTo(1));
        assertThat(document.getRelPartOf().size(), equalTo(1));
        assertThat(document.getRelIsRequiredBy().size(), equalTo(1));
        assertThat(document.getRelHasPart().size(), equalTo(1));
        assertThat(document.getRelReplaces().size(), equalTo(1));
        assertThat(document.getRelSource().size(), equalTo(1));
    }
}
