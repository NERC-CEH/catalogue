package uk.ac.ceh.gateway.catalogue.indexing;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DataciteService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
public class DataciteIndexingServiceTest {
    @Mock BundledReaderService<MetadataDocument> bundleReader;
    @Mock DataciteService datacite;
    private DataciteIndexingService service;
    
    @Before
    public void init() throws XPathExpressionException {
        MockitoAnnotations.initMocks(this);
        service = new DataciteIndexingService(bundleReader, datacite);
    }
    
    @Test
    public void checkThatIndexIsEmpty() throws DocumentIndexingException {
        //Given
        //Nothing
        
        //When
        boolean isEmpty = service.isIndexEmpty();
        
        //Then
        assertFalse("Expected the index to be empty", isEmpty);
    }
    
    @Test
    public void checkThatUpdatesDoiOfDocumentWhichRequestHasChanged() throws Exception {
        //Given
        String dataciteRequest = IOUtils.toString(getClass().getResource("datacite-date-request.xml"), "UTF-8");
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
        String dataciteRequest = IOUtils.toString(getClass().getResource("datacite-date-request.xml"), "UTF-8");
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
    public void checkThatLoopsOverIndexingDocuments() throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        //Given
        List<String> metadataIds = Arrays.asList("1");
        
        //When
        service.indexDocuments(metadataIds, "revision");
        
        //Then
        verify(bundleReader).readBundle("1", "revision");
    }
}
