package uk.ac.ceh.gateway.catalogue.gemini;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

@Slf4j
public class GeminiDocumentTest {
    private final String id = "c43818fc-61fb-455b-9714-072355597229";
    private final String rel1 = "https://example.com/rel/1";
    private final String doc1 = "https://example.com/doc/1";
    private final String doc2 = "https://example.com/doc/2";
    private final String doc3 = "https://example.com/doc/3";


    @Test
    void getDownloads() {
        //given
        val gemini = new GeminiDocument();
        gemini.setOnlineResources(List.of(
            OnlineResource.builder().function("download").build(),
            OnlineResource.builder().function("order").build(),
            OnlineResource.builder().function("something").build()
        ));

        //when
        val actual = gemini.getDownloads();

        //then
        assertThat(actual.size(), equalTo(2));
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
        document.setDescriptiveKeywords(List.of(
            DescriptiveKeywords.builder()
                .type("test")
                .keywords(List.of(
                    Keyword.builder().value("four").build(),
                    Keyword.builder().value("five").build()
                ))
                .build()
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
        val document = new ElterDocument();

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
        val document = new ElterDocument();
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
    public void unknownResourceStatus() {
        //given
        val document = new GeminiDocument();

        //when
        val actual = document.getResourceStatus();

        //then
        assertThat(actual, equalTo("Unknown"));
    }

    @Test
    public void accessLimitationAndResourceStatus() {
        //given
        val document = new GeminiDocument();
        document.setAccessLimitation(AccessLimitation.builder().build());

        //when
        val actual = document.getResourceStatus();

        //then
        assertThat(actual, equalTo("Unknown"));
    }

}
