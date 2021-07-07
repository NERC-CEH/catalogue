package uk.ac.ceh.gateway.catalogue.document;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtensionDocumentListingServiceTest {
    private ExtensionDocumentListingService service;

    @BeforeEach
    public void initMocks() {
        service = Mockito.spy(new ExtensionDocumentListingService());
    }

    @Test
    public void checkThatEmptyListCanBeRead() {
        //Given
        List<String> empty = Collections.emptyList();

        //When
        val docs = service.filterFilenames(empty);

        //Then
        assertTrue(docs.isEmpty());
    }

    @Test
    public void checkThatSingleMetadataFileDoesNotResultInDocument() {
        //Given
        val singleFile = Collections.singletonList("something.meta");

        //When
        val docs = service.filterFilenames(singleFile);

        //Then
        assertTrue(docs.isEmpty());
    }

    @Test
    public void checkThatSingleRawFileDoesNotResultInDocument() {
        //Given
        val singleFile = Collections.singletonList("something.raw");

        //When
        val docs = service.filterFilenames(singleFile);

        //Then
        assertTrue(docs.isEmpty());
    }

    @Test
    public void checkThatPairDoesResultInDocument() {
        //Given
        val singlePair = Arrays.asList("something.raw", "something.meta");

        //When
        val docs = service.filterFilenames(singlePair);

        //Then
        assertEquals(1, docs.size());
        assertEquals("something", docs.get(0));
    }

    @Test
    public void checkThatMoreThanOneFileResultsInDocument() {
        //Given
        val singlePair = Arrays.asList("some.raw", "some.meta", "some.img");

        //When
        val docs = service.filterFilenames(singlePair);

        //Then
        assertEquals(1, docs.size());
        assertEquals("some", docs.get(0));
    }

    @Test
    public void checkThatCanFindMultipleFiles() {
        //Given
        val multiplePairs = Arrays.asList("some.raw", "some.meta", "some1.raw", "some1.meta");

        //When
        val docs = service.filterFilenames(multiplePairs);

        //Then
        assertEquals(2, docs.size());
        assertTrue(docs.contains("some"));
        assertTrue(docs.contains("some1"));
    }

    @Test
    public void checkThatRawOnlyResultsInDocument() {
        //Given
        val raw = Collections.singletonList("some.raw");

        //When
        val docs = service.filterFilenamesEitherExtension(raw);

        //Then
        assertEquals(1, docs.size());
        assertEquals("some", docs.get(0));
    }

    @Test
    public void checkThatUnknownExtensionsDoesNotResultInDocument() {
        //Given
        val unknown = Collections.singletonList("some.unknown");

        //When
        val docs = service.filterFilenamesEitherExtension(unknown);

        //Then
        assertTrue(docs.isEmpty());
    }
}
