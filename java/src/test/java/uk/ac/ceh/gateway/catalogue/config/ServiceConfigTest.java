package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
}
