package uk.ac.ceh.gateway.catalogue.indexing.datacite;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataciteIndexingServiceTest {
    @Mock BundledReaderService<MetadataDocument> bundleReader;
    @Mock DataciteService datacite;
    private DataciteIndexingService service;

    @BeforeEach
    public void init() throws XPathExpressionException {
        service = new DataciteIndexingService(bundleReader, datacite);
    }

    @Test
    public void checkThatIndexIsEmpty() throws DocumentIndexingException {
        //Given
        //Nothing

        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertFalse(isEmpty);
    }

    @Test
    public void checkThatUpdatesDoiOfDocumentWhichRequestHasChanged() throws Exception {
        //Given
        String dataciteRequest = IOUtils.toString(
            Objects.requireNonNull(
                getClass().getResource("datacite-date-request.xml")
            ),
            StandardCharsets.UTF_8
        );
        GeminiDocument document = new GeminiDocument();
        when(datacite.isDatacited(document)).thenReturn(true);
        when(datacite.getDoiMetadata(document)).thenReturn(dataciteRequest);
        when(datacite.getDatacitationRequest(eq(document))).thenReturn("Different doi");

        //When
        service.indexDocument(document);

        //Then
        verify(datacite).updateDoiMetadata(document);
    }

    @Test
    public void checkThatDoesntUpdateDocumentWhichRequestHasntChanged() throws Exception {
        //Given
        String dataciteRequest = IOUtils.toString(
            Objects.requireNonNull(
                getClass().getResource("datacite-date-request.xml")
            ),
            StandardCharsets.UTF_8
        );
        GeminiDocument document = new GeminiDocument();
        when(datacite.isDatacited(document)).thenReturn(true);
        when(datacite.getDoiMetadata(document)).thenReturn(dataciteRequest);
        when(datacite.getDatacitationRequest(eq(document))).thenReturn(dataciteRequest);

        //When
        service.indexDocument(document);

        //Then
        verify(datacite, never()).updateDoiMetadata(document);
    }

    @Test
    public void checkThatRecordWhichIsNotDatacitedIsIgnored() throws Exception {
        //Given
        GeminiDocument document = new GeminiDocument();
        when(datacite.isDatacited(document)).thenReturn(false);

        //When
        service.indexDocument(document);

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
