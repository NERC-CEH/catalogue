package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

public class StateTranslatingMetadataInfoFactoryTest {

    @Test
    public void getDocumentInfo() {
        //Given
        StateTranslatingMetadataInfoFactory factory = new StateTranslatingMetadataInfoFactory();
        factory.put("private", "draft");
        GeminiDocument document = new GeminiDocument();
        TerraCatalogExt ext = new TerraCatalogExt("testOwner", "testGroup", "private", "protection");
        MetadataInfo expected = new MetadataInfo("application/xml", "draft", "GEMINI_DOCUMENT");
        
        //When
        MetadataInfo actual = factory.getDocumentInfo(document, ext);
        
        //Then
        assertThat("MetadataInfo 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void getDocumentInfoForUnknownStatus() {
        //Given
        StateTranslatingMetadataInfoFactory factory = new StateTranslatingMetadataInfoFactory();
        GeminiDocument document = new GeminiDocument();
        TerraCatalogExt ext = new TerraCatalogExt("testOwner", "testGroup", "internal", "protection");
        MetadataInfo expected = new MetadataInfo("application/xml", "draft", "GEMINI_DOCUMENT");
        
        //When
        MetadataInfo actual = factory.getDocumentInfo(document, ext);
        
        //Then
        assertThat("MetadataInfo 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
}