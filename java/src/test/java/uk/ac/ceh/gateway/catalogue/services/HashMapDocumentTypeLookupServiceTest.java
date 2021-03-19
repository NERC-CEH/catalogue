package uk.ac.ceh.gateway.catalogue.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

public class HashMapDocumentTypeLookupServiceTest {
    @Test
    public void checkThatGeminiDocumentIsAssignedCorrectType() {
        //Given
        HashMapDocumentTypeLookupService service = 
                new HashMapDocumentTypeLookupService()
                    .register("GEMINI_DOCUMENT", GeminiDocument.class);
        
        //When
        String type = service.getName(GeminiDocument.class);
        
        //Then
        assertEquals("Expected to find gemini document", "GEMINI_DOCUMENT", type);
    }
    
    @Test
    public void checkThatGeminiDocumentTypeReturnsCorrectClass() {
        //Given
        HashMapDocumentTypeLookupService service = 
                new HashMapDocumentTypeLookupService()
                    .register("GEMINI_DOCUMENT", GeminiDocument.class);
        
        //When
        Class<? extends MetadataDocument> clazz = service.getType("GEMINI_DOCUMENT");
        
        //Then
        assertEquals("Expected to find gemini document class", GeminiDocument.class, clazz);
    }
    
    @Test
    public void checkThatNameCanBeObtainedForAssignableType() {
        //Given
        HashMapDocumentTypeLookupService service = 
                new HashMapDocumentTypeLookupService()
                    .register("GEMINI_DOCUMENT", GeminiDocument.class);
        
        GeminiDocument subType = mock(GeminiDocument.class);
        
        //When
        String name = service.getName(subType.getClass());
        
        //Then
        assertEquals("Expected to get gemini_document for sub type", "GEMINI_DOCUMENT", name);        
    }
    
    @Test
    public void checkThatUnkownDocumentCantBeGivenAType() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            //Given
            HashMapDocumentTypeLookupService service =
                    new HashMapDocumentTypeLookupService();

            //When
            service.getType("Some random Giberish");

            //Then
            fail("Expected to fail with an illegalArgumentException");
        });
    }
    
    @Test
    public void checkThatUnknownClassTypeCantBeAssingedADocumentType() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            //Given
            HashMapDocumentTypeLookupService service =
                    new HashMapDocumentTypeLookupService();
            MetadataDocument unhandledMetadataDocument = mock(MetadataDocument.class);

            //When
            service.getName(unhandledMetadataDocument.getClass());

            //Then
            fail("Expectd to fail with an illegal arugment exception");
        });
    }    
}
