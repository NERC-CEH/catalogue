package uk.ac.ceh.gateway.catalogue.config;

import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.converters.Xml2WmsCapabilitiesMessageConverter;

/**
 *
 * @author cjohn
 */
public class WebConfigTest {
    private WebConfig configuration;
    
    @Before
    public void createWebConfig() {
        configuration = new WebConfig();
    }
    
    @Test
    public void checkThatRestTemplateHasXml2WmsCapabilitiesMessageConverter() throws XPathExpressionException {
        //Given
        //nothing
        
        //When
        RestTemplate template = configuration.restTemplate();
        
        //Then
        List<HttpMessageConverter<?>> converters = template.getMessageConverters();
        
        assertEquals("Expected to find a xml2wms capabilites message converter", 1,
                converters.stream()
                          .filter((c)-> c.getClass().equals(Xml2WmsCapabilitiesMessageConverter.class))
                          .count());
    }
}
