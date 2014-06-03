
package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.springframework.http.HttpInputMessage;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DescriptiveKeywords;
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
    public void canGetAlternateTitles() throws IOException {
       
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
                "alternateTitles.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<String> actual = document.getAlternateTitles();
        List<String> expected = Arrays.asList("First alternate title", "Second alternate title");
        Collections.sort(actual);
        Collections.sort(expected);
        
        //Then
        assertNotNull("Expected title to have content", actual);
        assertThat("Content of alternateTitles not as expected", actual, is(expected));
    }
    
    @Test
    public void canGetDatasetLanguage() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("language.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertNotNull("Expected language type not to be null", document.getDatasetLanguage());
        assertNotNull("Expected language code not to be null", document.getDatasetLanguage().getCodeListValue());
        assertNotNull("Expected language code list to not be null", document.getDatasetLanguage().getCodeList());
        assertFalse("Expected language code not be empty string", document.getDatasetLanguage().getCodeListValue().isEmpty());
        assertFalse("Expected language code list not be empty string", document.getDatasetLanguage().getCodeList().isEmpty());
    }
    
    @Test
    public void canGetTopicCategories() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("topicCategories.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<String> actual = document.getTopicCategories();
        List<String> expected = Arrays.asList("environment", "imageryBaseMapsEarthCover");
        Collections.sort(actual);
        Collections.sort(expected);
        
        //Then
        assertNotNull("Expected topicCategories to not be null", actual);
        assertThat("Content of topicCategories is not as expected", actual, is(expected));
    }
    
    @Test
    public void canGetUncitedKeywords() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("keywordsUncited.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<DescriptiveKeywords> descriptiveKeywords = document.getDescriptiveKeywords();
        
        //Then
        assertNotNull("Expected descriptiveKeywords not to be null", descriptiveKeywords);
        assertThat("descriptiveKeywords list should have 1 descriptiveKeywords entry", descriptiveKeywords.size(), is(1));
        
        //When
        List<String> actual = descriptiveKeywords.get(0).getKeywords();
        List<String> expected = Arrays.asList("Uncited1", "Uncited2");
        Collections.sort(actual);
        Collections.sort(expected);
        
        //Then
        assertNotNull("Expected keywords to not be null", actual);
        assertThat("Content of keywords is not as expected", actual, is(expected));
    }
    
}
