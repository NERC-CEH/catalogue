package uk.ac.ceh.gateway.catalogue.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import uk.ac.ceh.gateway.catalogue.ogc.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Xml2WmsCapabilitiesMessageConverterTest {
    private Xml2WmsCapabilitiesMessageConverter capabilitiesReader;

    @BeforeEach
    public void createXml2CapabilitiesConverter() throws XPathExpressionException {
        capabilitiesReader = new Xml2WmsCapabilitiesMessageConverter();
    }

    @Test
    public void canGetLayerNamesFromGetCapabilities() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("wmsGetCapabilitiesLayers.xml"));

        //When
        WmsCapabilities capabilities = capabilitiesReader.readInternal(WmsCapabilities.class, message);

        //Then
        List<String> layers = capabilities.getLayers()
                                          .stream()
                                          .map(Layer::getName)
                                          .collect(Collectors.toList());

        assertThat("Expected 3 layers", layers.size(), equalTo(3));
        assertThat("Expected three layers", layers, equalTo(Arrays.asList(
                "Layer 1", "Layer 2", "Layer 3")));
    }

    @Test
    public void canGetLayerTitlesFromGetCapabilities() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("wmsGetCapabilitiesLayers.xml"));

        //When
        WmsCapabilities capabilities = capabilitiesReader.readInternal(WmsCapabilities.class, message);

        //Then
        List<String> layers = capabilities.getLayers()
                                          .stream()
                                          .map(Layer::getTitle)
                                          .collect(Collectors.toList());

        assertThat("Expected 3 layers", layers.size(), equalTo(3));
        assertThat("Expected three layers", layers, equalTo(Arrays.asList(
                "Title 1", "Title 2", "Title 3")));
    }

    @Test
    public void canGetLegendFromLayerWithOnlyOnStyle() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("wmsGetCapabilitiesLayers.xml"));

        //When
        WmsCapabilities capabilities = capabilitiesReader.readInternal(WmsCapabilities.class, message);
        Layer firstLayer = capabilities.getLayers().get(0);

        //Then
        assertThat("Expected to find legend url on first layer", firstLayer.getLegendUrl(), equalTo("http://default/wms/legend.png"));
    }

    @Test
    public void canGetDefaultLegendFromLayerWithMultipleStylesDefined() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("wmsGetCapabilitiesLayers.xml"));

        //When
        WmsCapabilities capabilities = capabilitiesReader.readInternal(WmsCapabilities.class, message);
        Layer firstLayer = capabilities.getLayers().get(1);

        //Then
        assertThat("Expected to find default legend url on second layer", firstLayer.getLegendUrl(), equalTo("http://default/wms/default.png"));
    }

    @Test
    public void checkThatLayerWithNoStyleDoesNotReturnLegend() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("wmsGetCapabilitiesLayers.xml"));

        //When
        WmsCapabilities capabilities = capabilitiesReader.readInternal(WmsCapabilities.class, message);
        Layer firstLayer = capabilities.getLayers().get(2);

        //Then
        assertNull(firstLayer.getLegendUrl());
    }

    @Test
    public void canGetMapUrl() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("wmsGetCapabilitiesRequest.xml"));

        //When
        WmsCapabilities capabilities = capabilitiesReader.readInternal(WmsCapabilities.class, message);

        //Then
        assertThat("Expected to read map url", capabilities.getDirectMap(), equalTo("http://www.somewhere.com/GetMap?"));
    }

    @Test
    public void canGetFeatureInfoUrl() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("wmsGetCapabilitiesRequest.xml"));

        //When
        WmsCapabilities capabilities = capabilitiesReader.readInternal(WmsCapabilities.class, message);

        //Then
        assertThat("Expected to read map url", capabilities.getDirectFeatureInfo(), equalTo("http://www.somewhere.com/GetFeatureInfo?"));
    }
}
