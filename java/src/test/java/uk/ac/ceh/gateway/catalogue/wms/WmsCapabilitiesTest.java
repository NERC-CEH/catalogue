package uk.ac.ceh.gateway.catalogue.wms;

import org.junit.jupiter.api.Test;
import org.springframework.util.SerializationUtils;
import uk.ac.ceh.gateway.catalogue.ogc.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class WmsCapabilitiesTest {

    @Test
    void checkWmsCapabilitiesSerializable() {
        //given
        WmsCapabilities wmsCapabilities = new WmsCapabilities();
        wmsCapabilities.setDirectFeatureInfo("directFeatureInfo");
        wmsCapabilities.setDirectMap("directMap");
        Layer layer = new Layer();
        layer.setLegendUrl("legendUrl");
        layer.setName("name");
        layer.setTitle("title");
        wmsCapabilities.setLayers(List.of(layer));

        //when
        byte[] serialize = SerializationUtils.serialize(wmsCapabilities);
        WmsCapabilities deserialized = (WmsCapabilities) SerializationUtils.deserialize(serialize);

        //then
        assertThat(wmsCapabilities, equalTo(deserialized));
    }
}
