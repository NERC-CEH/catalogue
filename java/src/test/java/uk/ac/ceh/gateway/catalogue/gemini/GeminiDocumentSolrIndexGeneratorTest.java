package uk.ac.ceh.gateway.catalogue.gemini;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocumentSolrIndexGenerator.GeminiDocumentSolrIndex;
import uk.ac.ceh.gateway.catalogue.gemini.elements.CodeListItem;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DownloadOrder;

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
    
    @Test
    public void checkThatDescriptionIsTransferedToIndex() {
        //Given
        String description = "Once upon a time, there was a metadata record...";
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getDescription()).thenReturn(description);
        
        //When
        GeminiDocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my description", description, index.getDescription());
    }
    
    @Test
    public void checkThatResourceTypeIsTransferedToIndex() {
        //Given
        CodeListItem resourceType = CodeListItem
                .builder()
                .value("dataset")
                .build();
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getResourceType()).thenReturn(resourceType);
        
        //When
        GeminiDocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my resourceType", resourceType.getValue(), index.getResourceType());
    }
    
    @Test
    public void checkThatIsOglTrueIsTransferredToIndex(){
        //Given
        DownloadOrder downloadOrder = DownloadOrder
                .builder()
                .licenseUrl("http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain")
                .build();
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getDownloadOrder()).thenReturn(downloadOrder);
        
        //When
        GeminiDocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected isOgl to be true", true, index.getIsOgl());
    }
    
    @Test
    public void checkThatIsOglFalseIsTransferredToIndex(){
        //Given
        DownloadOrder downloadOrder = DownloadOrder
                .builder()
                .licenseUrl("http://I.am.a.non.ogl.license")
                .build();
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getDownloadOrder()).thenReturn(downloadOrder);
        
        //When
        GeminiDocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected isOgl to be false", false, index.getIsOgl());
    }

}
