package uk.ac.ceh.gateway.catalogue.converters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.MockHttpOutputMessage;
import uk.ac.ceh.gateway.catalogue.ogc.WmsFeatureInfo.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.WmsFeatureInfo;
import uk.ac.ceh.gateway.catalogue.ogc.WmsFeatureInfo.Layer.Feature;

public class WmsFeatureInfo2XmlMessageConverterTest {
    private WmsFeatureInfo2XmlMessageConverter converter;

    @BeforeEach
    public void init() {
        converter = new WmsFeatureInfo2XmlMessageConverter();
    }

    @Test
    public void checkThatCanProduceAMapServiceResponse() throws Exception {
        //Given
        MockHttpOutputMessage response = new MockHttpOutputMessage();
        WmsFeatureInfo info = new WmsFeatureInfo();
        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr1", "value1");
        Feature feature1 = new Feature();
        feature1.setAttributes(attributes);
        Layer layer1 = new Layer();
        layer1.setFeatures(Arrays.asList(feature1));
        info.setLayers(Arrays.asList(layer1));

        //When
        converter.writeInternal(info, response);

        //Then
        assertThat(response.getBodyAsString(), equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><FeatureInfoResponse><FIELDS attr1=\"value1\"/></FeatureInfoResponse>"));
    }
}
