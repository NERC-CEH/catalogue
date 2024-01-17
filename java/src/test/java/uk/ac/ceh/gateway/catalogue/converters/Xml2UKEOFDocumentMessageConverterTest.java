package uk.ac.ceh.gateway.catalogue.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Xml2UKEOFDocumentMessageConverterTest {
    private UkeofXml2EFDocumentMessageConverter ukeofReader;

    @BeforeEach
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
        assertTrue(document instanceof Activity);
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
        assertThat("Expected to be able to read document id", document.getEfMetadata().getFileIdentifier(), equalTo(UUID.fromString("00a1aab5-3e82-498a-a78f-b41e1955248b")));
    }
}
