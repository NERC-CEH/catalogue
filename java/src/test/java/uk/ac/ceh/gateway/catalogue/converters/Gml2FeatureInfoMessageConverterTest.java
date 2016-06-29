package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.xpath.XPathExpressionException;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpInputMessage;
import uk.ac.ceh.gateway.catalogue.ogc.FeatureInfo;
import uk.ac.ceh.gateway.catalogue.ogc.FeatureInfo.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.FeatureInfo.Layer.Feature;

/**
 *
 * @author cjohn
 */
public class Gml2FeatureInfoMessageConverterTest {
    private Gml2FeatureInfoMessageConverter reader;
    
    @Before
    public void init() throws XPathExpressionException {
        reader = new Gml2FeatureInfoMessageConverter();
    }
    
    @Test
    public void canGetMSGmlFromXML() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("msGMLOutput.xml"));
        
        //When
        FeatureInfo gml = reader.readInternal(FeatureInfo.class, message);
        
        //Then
        List<Layer> layers = gml.getLayers();
        Layer layer = layers.get(0);
        List<Feature> features = layer.getFeatures();
        Map<String,String> attributes = features.get(0).getAttributes();
        assertThat(layers.size(), is(1));
        assertThat(features.size(), is(1));
        assertThat(layer.getName(), is("SOMEDATA"));
        assertThat(attributes.size(), is(2));
        assertThat(attributes.get("ATTR1"), is("value1"));
        assertThat(attributes.get("ATTR2"), is("value2"));
    }
}
