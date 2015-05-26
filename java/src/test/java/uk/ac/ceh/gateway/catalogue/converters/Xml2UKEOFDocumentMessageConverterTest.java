package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import java.util.UUID;
import javax.xml.xpath.XPathExpressionException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpInputMessage;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;

/**
 *
 * @author cjohn
 */
public class Xml2UKEOFDocumentMessageConverterTest {
    private UkeofXml2EFDocumentMessageConverter ukeofReader;
    
    @Before
    public void createXml2UKEOFConverter() throws XPathExpressionException {
        ukeofReader = new UkeofXml2EFDocumentMessageConverter();
    }
    
    @Test
    public void canGetDocumentType() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("ukeofActivity.xml"));
        
        //When
        BaseMonitoringType document = (BaseMonitoringType) ukeofReader.readInternal(BaseMonitoringType.class, message);
        
        //Then
        assertTrue("Expected document type to be activity", document instanceof Activity);
    }
    
    @Test
    public void canGetDocumentTitle() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("ukeofActivity.xml"));
        
        //When
        BaseMonitoringType document = (BaseMonitoringType) ukeofReader.readInternal(BaseMonitoringType.class, message);
        
        //Then
        assertThat("Expected to be able to read document title", document.getName(), equalTo("UKEOF Title"));
    }
        
    @Test
    public void canGetDocumentDescription() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("ukeofActivity.xml"));
        
        //When
        BaseMonitoringType document = (BaseMonitoringType) ukeofReader.readInternal(BaseMonitoringType.class, message);
        
        //Then
        assertThat("Expected to be able to read document description", document.getDescription(), equalTo("Ukeof Description"));
    }
    
    @Test
    public void canGetDocumentID() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("ukeofActivity.xml"));
        
        //When
        BaseMonitoringType document = (BaseMonitoringType) ukeofReader.readInternal(BaseMonitoringType.class, message);
        
        //Then
        assertThat("Expected to be able to read document id", document.getMetadata().getFileIdentifier(), equalTo(UUID.fromString("00a1aab5-3e82-498a-a78f-b41e1955248b")));
    }
}
