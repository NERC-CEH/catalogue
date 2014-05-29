
package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;
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
    public void createGeminiDocumentConverter() throws XPathExpressionException {
        geminiReader = new Xml2GeminiDocumentMessageConverter();
    }

    @Test
    public void canGetId() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
                "9e7790ab-a37d-4918-8107-5c427798ca68.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertEquals("Expected to be able to read the id", "9e7790ab-a37d-4918-8107-5c427798ca68", document.getId());
    }

    @Test
    public void canGetTitle() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
                "9e7790ab-a37d-4918-8107-5c427798ca68.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertNotNull("Expected title to have content", document.getTitle());
    }

    @Test
    public void canGetAlternateTitle() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
                "9e7790ab-a37d-4918-8107-5c427798ca68.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertNotNull("Expected title to have content", document.getTitle());
    }
    
    @Test
    public void canGetLanguageCodeList() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("9e7790ab-a37d-4918-8107-5c427798ca68.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertNotNull("Expected languageCodeList to have content", document.getLanguageCodeList());
    }
    
//    @Test
//    public void canGetAbstract() throws IOException {
//        //Given
//        HttpInputMessage message = mock(HttpInputMessage.class);
//        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
//                "9e7790ab-a37d-4918-8107-5c427798ca68.xml"));
//        
//        //When
//        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
//        
//        //Then
//        assertNotNull("Expected abstract to have content", document.getDescription());
//    }
//
//    @Test
//    public void canGetTopicCategory() throws IOException {
//        //Given
//        HttpInputMessage message = mock(HttpInputMessage.class);
//        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
//                "9e7790ab-a37d-4918-8107-5c427798ca68.xml"));
//        
//        //When
//        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
//        
//        //Then
//        assertNotNull("Expected topicCategory to have content", document.getTopicCategory());
//    }
//
//    @Test
//    public void canGetTemporalExtentBegin() throws IOException {
//        //Given
//        HttpInputMessage message = mock(HttpInputMessage.class);
//        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
//                "9e7790ab-a37d-4918-8107-5c427798ca68.xml"));
//        
//        //When
//        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
//        
//        //Then
//        assertNotNull("Expected temporalExtent-begin to have content", document.getTemporalExtentBegin());
//    }
//
//    @Test
//    public void canGetTemporalExtentEnd() throws IOException {
//        //Given
//        HttpInputMessage message = mock(HttpInputMessage.class);
//        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
//                "9e7790ab-a37d-4918-8107-5c427798ca68.xml"));
//        
//        //When
//        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
//        
//        //Then
//        assertNotNull("Expected temporalExtent-end to have content", document.getTemporalExtentEnd());
//    }
}
