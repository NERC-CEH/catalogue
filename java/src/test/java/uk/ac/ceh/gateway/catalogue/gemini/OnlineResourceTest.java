package uk.ac.ceh.gateway.catalogue.gemini;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OnlineResourceTest {

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

    @ParameterizedTest
    @ValueSource(strings = {
        "https://order-eidc.ceh.ac.uk/resources/ABCDEFGH/order",
        "https://data-package.ceh.ac.uk/data/ABCDEFGH.zip",
        "https://catalogue.ceh.ac.uk/datastore/eidchub/ABCDEFGH/"
    })
    public void eidcDistributionUrlsAreRecognised(String url) {
        //Given
        OnlineResource resource = OnlineResource.builder().url(url).build();

        //When/Then
        assertTrue(resource.isEidcDistribution());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://example.com/data.zip",
        "https://data.ceda.ac.uk/eidc/ABCDEFGH",
        "http://data-package.ceh.ac.uk/data/ABCDEFGH.zip",
        "https://catalogue.ceh.ac.uk/documents/ABCDEFGH"
    })
    public void nonEidcDistributionUrlsAreNotRecognised(String url) {
        //Given
        OnlineResource resource = OnlineResource.builder().url(url).build();

        //When/Then
        assertFalse(resource.isEidcDistribution());
    }

    @Test
    public void missingUrlIsNotAnEidcDistribution() {
        //Given a resource whose url was absent from the document
        OnlineResource resource = OnlineResource.builder().function("offlineAccess").build();

        //When/Then
        assertFalse(resource.isEidcDistribution());
    }

}
