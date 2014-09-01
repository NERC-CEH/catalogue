package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpInputMessage;
import uk.ac.ceh.gateway.catalogue.ukeof.UKEOFDocument;

/**
 *
 * @author cjohn
 */
public class Xml2UKEOFDocumentMessageConverterTest {
    private Xml2UKEOFDocumentMessageConverter ukeofReader;
    
    @Before
    public void createXml2UKEOFConverter() throws XPathExpressionException {
        ukeofReader = new Xml2UKEOFDocumentMessageConverter();
    }
    
    @Test
    public void canGetDocumentType() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("ukeofActivity.xml"));
        
        //When
        UKEOFDocument document = ukeofReader.readInternal(UKEOFDocument.class, message);
        
        //Then
        assertThat("Expected document type to be activity", document.getType(), equalTo("activity"));
    }
    
    @Test
    public void canGetDocumentTitle() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("ukeofActivity.xml"));
        
        //When
        UKEOFDocument document = ukeofReader.readInternal(UKEOFDocument.class, message);
        
        //Then
        assertThat("Expected to be able to read document title", document.getTitle(), equalTo("UKEOF Title"));
    }
        
    @Test
    public void canGetDocumentDescription() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("ukeofActivity.xml"));
        
        //When
        UKEOFDocument document = ukeofReader.readInternal(UKEOFDocument.class, message);
        
        //Then
        assertThat("Expected to be able to read document description", document.getDescription(), equalTo("Ukeof Description"));
    }
    
    @Test
    public void canGetDocumentID() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("ukeofActivity.xml"));
        
        //When
        UKEOFDocument document = ukeofReader.readInternal(UKEOFDocument.class, message);
        
        //Then
        assertThat("Expected to be able to read document id", document.getId(), equalTo("00a1aab5-3e82-498a-a78f-b41e1955248b"));
    }
}
