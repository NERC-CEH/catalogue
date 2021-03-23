package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.ac.ceh.gateway.catalogue.model.*;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExceptionControllerHandlerTest {
    private ExceptionControllerHandler controller;
    
    @BeforeEach
    public void setup() {
        this.controller = new ExceptionControllerHandler();
    }
    
    @Test
    public void checkThatExternalResourceFailureExceptionIsWrapped() {
        //Given
        String mess = "My exception message";
        ExternalResourceFailureException ex = mock(ExternalResourceFailureException.class);
        when(ex.getMessage()).thenReturn(mess);
        
        //When
        ErrorResponse res = (ErrorResponse) controller.handleExternalResourceFailureException(ex).getBody();
        
        //Then
        assertThat("Expected message to be pulled of exception", res.getMessage(), equalTo(mess));
    }
    
    @Test
    public void checkThatNotFoundExceptionsAreWrapped() {
        //Given
        String mess = "no online resource";
        ResourceNotFoundException ex = mock(ResourceNotFoundException.class);
        when(ex.getMessage()).thenReturn(mess);
        
        //When
        ErrorResponse res = (ErrorResponse) controller.handleNotFoundExceptions(ex).getBody();
        
        //Then
        assertThat("Expected message to be pulled of exception", res.getMessage(), equalTo(mess));
    }
    
    @Test
    public void checkThatURISyntaxExceptionReturnsImage() {
        //Given
        URISyntaxException ex = mock(URISyntaxException.class);
        
        //When
        ResponseEntity response = controller.handleURISyntaxException(ex);
        
        //Then
        assertResponseImageExists(response);
    }
    
    @Test
    public void checkThatTransparentExceptionReturnsImage() {
        //Given
        TransparentProxyException ex = mock(TransparentProxyException.class);
        
        //When
        ResponseEntity response = controller.handleTransparentProxyException(ex);
        
        //Then
        assertResponseImageExists(response);
    }
    
    @Test
    public void checkThatMissingLegendExceptionReturnsImage() {
        //Given
        LegendGraphicMissingException ex = mock(LegendGraphicMissingException.class);
        
        //When
        ResponseEntity response = controller.handleLegendGraphicMissingException(ex);
        
        //Then
        assertResponseImageExists(response);
    }
    
    @Test
    public void checkThatUpstreamInvalidMediaTypeExceptionReturnsImage() {
        //Given
        UpstreamInvalidMediaTypeException ex = mock(UpstreamInvalidMediaTypeException.class);
        
        //When
        ResponseEntity response = controller.handleUpstreamInvalidMediaTypeException(ex);
        
        //Then
        assertResponseImageExists(response);
    }
        
    private void assertResponseImageExists(ResponseEntity response) {
        HttpHeaders headers = response.getHeaders();
        ClassPathResource body = (ClassPathResource)response.getBody();
        
        //Then
        assertTrue(body.exists());
        assertThat(headers.getContentType(), equalTo(MediaType.IMAGE_PNG));
    }   
}
