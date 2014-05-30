
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
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("id.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertEquals("Expected to be able to read the id", "9e7790ab-a37d-4918-8107-5c427798ca68", document.getId());
        assertFalse("Expected id to not be empty string", document.getId().isEmpty());
    }

    @Test
    public void canGetTitle() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
                "title.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertNotNull("Expected title to have content", document.getTitle());
        assertFalse("Expected title to not be empty string", document.getTitle().isEmpty());
    }

    @Test
    public void canGetAlternateTitle() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
                "alternateTitle.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertNotNull("Expected title to have content", document.getAlternateTitle());
        assertFalse("Expected alternate title to not be empty string", document.getAlternateTitle().isEmpty());
    }
    
    @Test
    public void canGetDatasetLanguage() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("language.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertNotNull("Expected languageCodeList to have content", document.getDatasetLanguage());
        assertNotNull("Expected language code list to not be null", document.getDatasetLanguage().getCodeList());
        assertFalse("Expected language code list to not be empty string", document.getDatasetLanguage().getCodeList().isEmpty());
        assertNotNull("Expected language code list value to not be null", document.getDatasetLanguage().getCodeListValue());
        assertFalse("Expected language code list value to not be empty string", document.getDatasetLanguage().getCodeListValue().isEmpty());
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
