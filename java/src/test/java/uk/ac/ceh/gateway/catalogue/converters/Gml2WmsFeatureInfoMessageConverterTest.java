package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpInputMessage;
import uk.ac.ceh.gateway.catalogue.ogc.WmsFeatureInfo;
import uk.ac.ceh.gateway.catalogue.ogc.WmsFeatureInfo.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.WmsFeatureInfo.Layer.Feature;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class Gml2WmsFeatureInfoMessageConverterTest {
    private Gml2WmsFeatureInfoMessageConverter reader;
    
    @Before
    public void init() throws XPathExpressionException {
        reader = new Gml2WmsFeatureInfoMessageConverter();
    }
    
    @Test
    public void canGetMSGmlFromXML() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("msGMLOutput.xml"));
        
        //When
        WmsFeatureInfo gml = reader.readInternal(WmsFeatureInfo.class, message);
        
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
