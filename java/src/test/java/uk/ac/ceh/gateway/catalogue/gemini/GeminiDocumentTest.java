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

@Slf4j
public class GeminiDocumentTest {
    private final String id = "c43818fc-61fb-455b-9714-072355597229";
    private final String rel1 = "https://example.com/rel/1";
    private final String doc1 = "https://example.com/doc/1";
    private final String doc2 = "https://example.com/doc/2";
    private final String doc3 = "https://example.com/doc/3";

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
        document.setRelatedRecords(List.of(
            new RelatedRecord(rel1, "1", doc1, "", "")
        ));
        document.setRelationships(Set.of(
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
    public void testGetIncomingCitationCount() {
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

}
