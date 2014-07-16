package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;

public class StateTranslatingMetadataInfoFactoryTest {

    @Test
    public void getDocumentInfo() {
        //Given
        Map<String, String> stateTransalation = new HashMap<>();
        stateTransalation.put("private", "draft");
        TerraCatalogDocumentInfoFactory<MetadataInfo> factory = new StateTranslatingMetadataInfoFactory(stateTransalation);
        GeminiDocument document = new GeminiDocument();
        TerraCatalogExt ext = new TerraCatalogExt("testOwner", "testGroup", "private", "protection");
        MetadataInfo expected = new MetadataInfo("testGroup", null, "draft");
        
        //When
        MetadataInfo actual = factory.getDocumentInfo(document, ext);
        
        //Then
        assertThat("MetadataInfo 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void getDocumentInfoForUnknownStatus() {
        //Given
        Map<String, String> stateTransalation = new HashMap<>();
        stateTransalation.put("default", "draft");
        TerraCatalogDocumentInfoFactory<MetadataInfo> factory = new StateTranslatingMetadataInfoFactory(stateTransalation);
        GeminiDocument document = new GeminiDocument();
        TerraCatalogExt ext = new TerraCatalogExt("testOwner", "testGroup", "internal", "protection");
        MetadataInfo expected = new MetadataInfo("testGroup", null, "draft");
        
        //When
        MetadataInfo actual = factory.getDocumentInfo(document, ext);
        
        //Then
        assertThat("MetadataInfo 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
}
