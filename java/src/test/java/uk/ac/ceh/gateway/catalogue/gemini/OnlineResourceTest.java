package uk.ac.ceh.gateway.catalogue.gemini;

import org.junit.jupiter.api.Test;
import org.springframework.util.SerializationUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OnlineResourceTest {

    @Test
    void checkOnlineResourceCanBeSerialized() {
        //given
        OnlineResource onlineResource = OnlineResource.builder()
            .url("url")
            .description("description")
            .function("function")
            .build();

        //when
        byte[] serialize = SerializationUtils.serialize(onlineResource);
        OnlineResource deserialized = (OnlineResource) SerializationUtils.deserialize(serialize);

        //then
        assertThat(onlineResource, equalTo(deserialized));
    }

    @Test
    public void wmsUrlReturnsGetCapabilitiesType() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("https://wms.com?REQUEST=GetCapabilities&SERVICE=WMS&").build();

        //When
        OnlineResource.Type type = resource.getType();

        //Then
        assertEquals(OnlineResource.Type.WMS_GET_CAPABILITIES, type);
    }

    @Test
    public void caseForGetCapabilitiesDoesntMatter() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("https://wms.com?request=getcapabilities&SERVICE=WMS").build();

        //When
        OnlineResource.Type type = resource.getType();

        //Then
        assertEquals(OnlineResource.Type.WMS_GET_CAPABILITIES, type);
    }

    @Test
    public void urlWithGetCapabilitesInsideItIsNotFlagged() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("https://www.google.com/getcapabilities/somethingelse").build();

        //When
        OnlineResource.Type type = resource.getType();

        //Then
        assertEquals(OnlineResource.Type.OTHER, type);
    }

    @Test
    public void urlWithParameterAfterAmpersandMatchesGetCapabilites() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("https://wms.com?SERVICE=WMS&REQUEST=GetCapabilities&").build();

        //When
        OnlineResource.Type type = resource.getType();

        //Then
        assertEquals(OnlineResource.Type.WMS_GET_CAPABILITIES, type);
    }

}
