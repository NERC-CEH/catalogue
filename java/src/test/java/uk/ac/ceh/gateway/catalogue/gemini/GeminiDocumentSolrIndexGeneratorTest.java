package uk.ac.ceh.gateway.catalogue.gemini;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocumentSolrIndexGenerator.GeminiDocumentSolrIndex;

/**
 *
 * @author cjohn
 */
public class GeminiDocumentSolrIndexGeneratorTest {
    private GeminiDocumentSolrIndexGenerator generator;
    
    @Before
    public void createGeminiDocumentSolrIndexGenerator() {
        generator = new GeminiDocumentSolrIndexGenerator();
    }
    
    @Test
    public void checkThatTitleIsTransferedToIndex() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getTitle()).thenReturn("my gemini document");
        
        //When
        GeminiDocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my title", "my gemini document", index.getTitle());
    }
    
    @Test
    public void checkThatTitleIdTransferedToIndex() {
        //Given
        String id = "some crazy long, hard to rememember, number";
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn(id);
        
        //When
        GeminiDocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my id", id, index.getIdentifier());
    }
}
