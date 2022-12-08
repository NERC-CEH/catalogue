package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class GeminiDocumentTest {
    private final String id = "c43818fc-61fb-455b-9714-072355597229";

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
        Citation incomingCitations = Citation.builder().name("foo").build();
        List<Supplemental> incomingCitations = new ArrayList<>();
        incomingCitations.add(incomingCitation);
        document.setSupplemental(incomingCitations);
        long expected = 1;

        // When
        long output = document.getIncomingCitationCount();

        // Then
        assertThat(output, is(expected));
    }

    @Test
    public void testGetIncomingCitationCount_NoCitations() {
        // Given
        GeminiDocument document = new GeminiDocument();
        List<Supplemental> incomingCitations = new ArrayList<>();
        document.setSupplemental(incomingCitations);
        long expected = 0;

        // When
        long output = document.getIncomingCitationCount();

        // Then
        assertThat(output, is(expected));
    }
}