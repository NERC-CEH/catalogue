
package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.springframework.http.HttpInputMessage;
/**
 *
 * @author cjohn
 */
public class Xml2GeminiDocumentMessageConverterTest {
    private Xml2GeminiDocumentMessageConverter geminiReader;
    
    @Before
    public void createGeminiDocumentConverter() {
        geminiReader = new Xml2GeminiDocumentMessageConverter();
    }

    @Test
    public void canReadXmlContent() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
                "fc77c9b3-570d-4314-82ba-bc914538a748.xml"));
        
        //When
        GeminiDocument document = geminiReader.read(GeminiDocument.class, message);
        
        //Then
        assertEquals("Expected to be able to read the id", "fc77c9b3-570d-4314-82ba-bc914538a748", document.getId());
    }
}
