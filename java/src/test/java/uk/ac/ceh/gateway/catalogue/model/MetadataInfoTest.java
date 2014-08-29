package uk.ac.ceh.gateway.catalogue.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.ukeof.UKEOFDocument;

/**
 *
 * @author cjohn
 */
public class MetadataInfoTest {
    @Test
    public void checkThatMetadataInfoCanParseMediaType() {
        //Given
        MediaType type = MediaType.IMAGE_JPEG;
        MetadataInfo info = new MetadataInfo();
        info.setRawType(type.toString());
        
        //When
        MediaType infoType = info.getRawMediaType();
        
        //Then
        assertEquals("Expected the mediatypes to be equal", type, infoType);
    }
    
    @Test
    public void canHideMediaTypeFromMetadataInfo() {
        //Given
        MetadataInfo info = new MetadataInfo();
        info.setRawType("application/xml");
        
        //When
        info.hideMediaType();
        
        //Then
        assertNull("Expected no media type to be specified", info.getRawType());
    }
    
    @Test
    public void checkThatGeminiDocumentIsAssignedCorrectType() {
        //Given
        MetadataInfo info = new MetadataInfo();
        
        //When
        info.setDocumentClass(GeminiDocument.class);
        
        //Then
        assertEquals("Expected to find gemini document", "GEMINI_DOCUMENT", info.getDocumentType());
    }
    
    @Test
    public void checkThatGeminiDocumentTypeReturnsCorrectClass() {
        //Given
        MetadataInfo info = new MetadataInfo("", "dataset","GEMINI_DOCUMENT");
        
        //When
        Class<? extends MetadataDocument> clazz = info.getDocumentClass();
        
        //Then
        assertEquals("Expected to find gemini document class", GeminiDocument.class, clazz);
    }
    
        
    @Test
    public void checkThatUKEOFDocumentIsAssignedCorrectType() {
        //Given
        MetadataInfo info = new MetadataInfo();
        
        //When
        info.setDocumentClass(UKEOFDocument.class);
        
        //Then
        assertEquals("Expected to find ukeof document", "UKEOF_DOCUMENT", info.getDocumentType());
    }
    
    @Test
    public void checkThatUKEOFDocumentTypeReturnsCorrectClass() {
        //Given
        MetadataInfo info = new MetadataInfo("", "dataset","UKEOF_DOCUMENT");
        
        //When
        Class<? extends MetadataDocument> clazz = info.getDocumentClass();
        
        //Then
        assertEquals("Expected to find ukeof document class", UKEOFDocument.class, clazz);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void checkThatUnkownDocumentCantBeGivenAType() {
        //Given
        MetadataInfo info = new MetadataInfo();
        info.setDocumentType("Some Random Giberish Type");
        
        //When
        info.getDocumentClass();
        
        //Then
        fail("Expected to fail with an illegalArgumentException");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void checkThatUnknownClassTypeCantBeAssingedADocumentType() {
        //Given
        MetadataDocument unhandledMetadataDocument = mock(MetadataDocument.class);
        MetadataInfo info = new MetadataInfo();
        
        //When
        info.setDocumentClass(unhandledMetadataDocument.getClass());
        
        //Then
        fail("Expectd to fail with an illegal arugment exception");
    }
}
