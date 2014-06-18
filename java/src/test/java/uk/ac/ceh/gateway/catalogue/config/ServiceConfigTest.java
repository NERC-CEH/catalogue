package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import javax.xml.xpath.XPathExpressionException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentBundleService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;

/**
 *
 * @author cjohn
 */
public class ServiceConfigTest {
    @Mock EventBus bus;
    @Mock ObjectMapper jacksonMapper;
    
    private ServiceConfig services;
    
    @Before
    public void createServiceConfig() {
        MockitoAnnotations.initMocks(this);
        services = spy(new ServiceConfig());
        services.jacksonMapper = jacksonMapper;
        services.bus = bus;
    }
    
    @Test
    public void checkDocumentInfoFactorySetsMediaType() {
        //Given
        DocumentInfoFactory<GeminiDocument, MetadataInfo> documentInfoFactory = services.documentInfoFactory();
        GeminiDocument document = mock(GeminiDocument.class);
        MediaType type = MediaType.TEXT_HTML;
        
        //When
        MetadataInfo info = documentInfoFactory.createInfo(document, type);
        
        //Then
        assertEquals("Expected to find the text/html on metadata info", "text/html", info.getRawType());
    }
    
    @Test
    public void checkDocumentCanHaveMetadataBundled() {
        //Given
        DocumentBundleService<GeminiDocument, MetadataInfo> documentBundleService = services.documentBundleService();
        GeminiDocument document = new GeminiDocument();
        MetadataInfo info = mock(MetadataInfo.class);
        
        //When
        GeminiDocument bundled = documentBundleService.bundle(document, info);
        
        //Then
        assertEquals("Expected to find the metadata bundled", info, bundled.getMetadata());
    }
    
    @Test
    public void isTheMediaTypeObscuredWhenBundled() {
        //Given
        DocumentBundleService<GeminiDocument, MetadataInfo> documentBundleService = services.documentBundleService();
        GeminiDocument document = mock(GeminiDocument.class);
        MetadataInfo info = mock(MetadataInfo.class);
        
        //When
        documentBundleService.bundle(document, info);
        
        //Then
        verify(info).hideMediaType();
    }
    
    @Test
    public void checkThatAProvidedMediaTypeIsOverwritten() {
        //Given
        DocumentInfoFactory<GeminiDocument, MetadataInfo> documentInfoFactory = services.documentInfoFactory();
        GeminiDocument documentWithMetadataInfo = mock(GeminiDocument.class);
        MetadataInfo providedInfo = new MetadataInfo();
        //set the value of the raw type. This needs to be hidden
        providedInfo.setRawType(MediaType.IMAGE_PNG_VALUE);
        when(documentWithMetadataInfo.getMetadata()).thenReturn(providedInfo);
        
        //When
        MetadataInfo metadataInfo = documentInfoFactory.createInfo(documentWithMetadataInfo, MediaType.TEXT_HTML);
        
        //Then
        assertEquals("Expected to find the set media type", MediaType.TEXT_HTML, metadataInfo.getRawMediaType());
    }
    
    @Test
    public void checkIfNoMetadataDocumentIsPresentInGeminiANewOneIsCreated() {
        //Given
        DocumentInfoFactory<GeminiDocument, MetadataInfo> documentInfoFactory = services.documentInfoFactory();
        GeminiDocument documentWithMetadataInfo = mock(GeminiDocument.class);
        when(documentWithMetadataInfo.getMetadata()).thenReturn(null);
        
        //When
        MetadataInfo metadataInfo = documentInfoFactory.createInfo(documentWithMetadataInfo, MediaType.IMAGE_PNG);
        
        //Then
        assertNotNull("Didn't expect the metadata info to be null", metadataInfo);
        assertEquals("Expected to find image png media type", MediaType.IMAGE_PNG, metadataInfo.getRawMediaType());
    }
    
    @Test
    public void checkThatIndexingServiceIsRequestedToBeIndexedAfterCreation() throws XPathExpressionException {
        //Given
        doNothing().when(services).performReindexIfNothingIsIndexed(any(SolrIndexingService.class));
        
        //When
        SolrIndexingService<GeminiDocument> documentIndexingService = services.documentIndexingService();
        
        //Then
        verify(services).performReindexIfNothingIsIndexed(documentIndexingService);
    }
    
    @Test
    public void checkThatIndexingServiceIsReindexedIfEmpty() throws DocumentIndexingException {
        //Given
        SolrIndexingService<GeminiDocument> documentIndexingService = mock(SolrIndexingService.class);
        when(documentIndexingService.isIndexEmpty()).thenReturn(true);
        
        //When
        services.performReindexIfNothingIsIndexed(documentIndexingService);
        
        //Then
        verify(documentIndexingService).rebuildIndex();
    }
    
    @Test
    public void checkThatIndexingServiceIsNotReindexIfPopulated() throws DocumentIndexingException {
        //Given
        SolrIndexingService<GeminiDocument> documentIndexingService = mock(SolrIndexingService.class);
        when(documentIndexingService.isIndexEmpty()).thenReturn(false);
        
        //When
        services.performReindexIfNothingIsIndexed(documentIndexingService);
        
        //Then
        verify(documentIndexingService, never()).rebuildIndex();
    }
    
    @Test
    public void checkThatDocumentExceptionWhenReindexingIsPostedToEventBus() throws DocumentIndexingException {
        //Given
        DocumentIndexingException documentIndexingException = new DocumentIndexingException("Failed to check if index is empty");
        SolrIndexingService<GeminiDocument> documentIndexingService = mock(SolrIndexingService.class);
        when(documentIndexingService.isIndexEmpty()).thenThrow(documentIndexingException);
        
        //When
        services.performReindexIfNothingIsIndexed(documentIndexingService);
        
        //Then
        verify(bus).post(documentIndexingException);
    }
}
