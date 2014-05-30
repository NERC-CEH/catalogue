package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.services.DocumentBundleService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;

/**
 *
 * @author cjohn
 */
public class ServiceConfigTest {
    private ServiceConfig services;
    
    @Before
    public void createServiceConfig() {
        services = new ServiceConfig();
        services.jacksonMapper = mock(ObjectMapper.class);
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
    
}
