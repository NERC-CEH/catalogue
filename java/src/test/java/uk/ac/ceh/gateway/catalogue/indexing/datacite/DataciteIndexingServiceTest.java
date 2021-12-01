package uk.ac.ceh.gateway.catalogue.indexing.datacite;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DataciteIndexingServiceTest {
    @Mock BundledReaderService<MetadataDocument> bundleReader;
    @Mock DataciteService datacite;
    private DataciteIndexingService service;

    @BeforeEach
    void init() {
        service = new DataciteIndexingService(bundleReader, datacite);
    }

    @Test
    void checkThatIndexIsEmpty() throws DocumentIndexingException {
        //Given
        //Nothing

        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertFalse(isEmpty);
    }

    @Test
    void checkThatUpdatesDoiOfDocumentWhichRequestHasChanged() throws Exception {
        //Given
        String dataciteRequest = IOUtils.toString(
            Objects.requireNonNull(
                getClass().getResource("datacite-date-request.xml")
            ),
            StandardCharsets.UTF_8
        );
        GeminiDocument document = new GeminiDocument();
        given(datacite.isDatacited(document))
            .willReturn(true);
        given(datacite.getDoiMetadata(document))
            .willReturn(dataciteRequest);
        given(datacite.getDatacitationRequest(eq(document)))
            .willReturn("Different doi");
        given(bundleReader.readBundle("document", "latest"))
            .willReturn(document);


        //When
        service.indexDocuments(List.of("document"), "latest");

        //Then
        verify(datacite).updateDoiMetadata(document);
    }

    @Test
    void checkThatDoesntUpdateDocumentWhichRequestHasNotChanged() throws Exception {
        //Given
        String dataciteRequest = IOUtils.toString(
            Objects.requireNonNull(
                getClass().getResource("datacite-date-request.xml")
            ),
            StandardCharsets.UTF_8
        );
        GeminiDocument document = new GeminiDocument();
        given(datacite.isDatacited(document))
            .willReturn(true);
        given(datacite.getDoiMetadata(document))
            .willReturn(dataciteRequest);
        given(datacite.getDatacitationRequest(eq(document)))
            .willReturn(dataciteRequest);
        given(bundleReader.readBundle("document", "latest"))
            .willReturn(document);

        //When
        service.indexDocuments(List.of("document"), "latest");

        //Then
        verify(datacite, never()).updateDoiMetadata(document);
    }

    @Test
    @SneakyThrows
    void checkThatRecordWhichIsNotDatacitedIsIgnored() {
        //Given
        GeminiDocument document = new GeminiDocument();
        given(datacite.isDatacited(document)).willReturn(false);
        given(bundleReader.readBundle("document", "latest"))
            .willReturn(document);

        //When
        service.indexDocuments(List.of("document"), "latest");

        //Then
        verify(datacite, never()).updateDoiMetadata(document);
    }

    @Test
    public void checkThatLoopsOverIndexingDocuments() throws IOException, UnknownContentTypeException, PostProcessingException {
        //Given
        List<String> metadataIds = Collections.singletonList("1");

        //When
        service.indexDocuments(metadataIds, "revision");

        //Then
        verify(bundleReader).readBundle("1", "revision");
    }
}
