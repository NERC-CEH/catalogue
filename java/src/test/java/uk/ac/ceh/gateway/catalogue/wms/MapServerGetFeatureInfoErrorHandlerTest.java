package uk.ac.ceh.gateway.catalogue.wms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import uk.ac.ceh.gateway.catalogue.model.MapServerException;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.MAPSERVER_GML_VALUE;

public class MapServerGetFeatureInfoErrorHandlerTest {
    private MapServerGetFeatureInfoErrorHandler handler;

    @BeforeEach
    public void init() {
        handler = new MapServerGetFeatureInfoErrorHandler();
    }

    @Test
    public void checkThatGMLMediaTypeIsFine() {
        //Given
        ClientHttpResponse response = mock(ClientHttpResponse.class, RETURNS_DEEP_STUBS);
        when(response.getHeaders().getContentType()).thenReturn(MediaType.parseMediaType(MAPSERVER_GML_VALUE));

        //When
        boolean isError = handler.hasError(response);

        //Then
        assertFalse(isError);
    }


    @Test
    public void checkThatXMLMediaTypeIsFine() {
        //Given
        ClientHttpResponse response = mock(ClientHttpResponse.class, RETURNS_DEEP_STUBS);
        when(response.getHeaders().getContentType()).thenReturn(MediaType.TEXT_XML);

        //When
        boolean isError = handler.hasError(response);

        //Then
        assertTrue(isError);
    }

    @Test
    public void checkThatErrorThrowsException() {
        Assertions.assertThrows(MapServerException.class, () -> {
            //Given
            ClientHttpResponse response = mock(ClientHttpResponse.class);
            given(response.getStatusCode()).willReturn(HttpStatusCode.valueOf(400));
            given(response.getHeaders()).willReturn(HttpHeaders.EMPTY);
            ByteArrayInputStream inputStream = new ByteArrayInputStream("Danger!".getBytes());
            given(response.getBody()).willReturn(inputStream);

            //When
            handler.handleError(response);

            //Then
            fail("Expected an exception");
        });
    }
}
