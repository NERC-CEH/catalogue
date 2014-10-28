package uk.ac.ceh.gateway.catalogue.controllers;

import java.net.URISyntaxException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.model.ErrorResponse;
import uk.ac.ceh.gateway.catalogue.model.ExternalResourceFailureException;
import uk.ac.ceh.gateway.catalogue.model.LegendGraphicMissingException;
import uk.ac.ceh.gateway.catalogue.model.NoSuchOnlineResourceException;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxyException;
import uk.ac.ceh.gateway.catalogue.model.UpstreamInvalidMediaTypeException;

/**
 *
 * @author cjohn
 */
@ControllerAdvice
public class ExceptionControllerHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
        GitFileNotFoundException.class,
        NoSuchOnlineResourceException.class
    })
    @ResponseBody
    public ErrorResponse handleNotFoundExceptions(Exception ex) {
        return new ErrorResponse(ex.getMessage());
    }
    
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(ExternalResourceFailureException.class)
    @ResponseBody
    public ErrorResponse handleExternalResourceFailureException(ExternalResourceFailureException ex) {
        return new ErrorResponse(ex.getMessage());
    }
    
    @ExceptionHandler(TransparentProxyException.class)
    @ResponseBody
    public ResponseEntity handleTransparentProxyException(TransparentProxyException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        
        return new ResponseEntity<>(
                new ClassPathResource("proxy-failure.png"),
                headers,
                HttpStatus.GATEWAY_TIMEOUT);
    }
    
    @ExceptionHandler(URISyntaxException.class)
    @ResponseBody
    public ResponseEntity handleURISyntaxException(URISyntaxException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        
        return new ResponseEntity<>(
                new ClassPathResource("proxy-invalid-resource.png"),
                headers,
                HttpStatus.BAD_GATEWAY);
    }
    
    @ExceptionHandler(UpstreamInvalidMediaTypeException.class)
    @ResponseBody
    public ResponseEntity handleUpstreamInvalidMediaTypeException(UpstreamInvalidMediaTypeException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        
        return new ResponseEntity<>(
                new ClassPathResource("proxy-invalid-response.png"),
                headers,
                HttpStatus.BAD_GATEWAY);
    }
    
    @ExceptionHandler(LegendGraphicMissingException.class)
    @ResponseBody
    public ResponseEntity handleLegendGraphicMissingException(LegendGraphicMissingException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        
        return new ResponseEntity<>(
                new ClassPathResource("legend-not-found.png"),
                headers,
                HttpStatus.NOT_FOUND);
    }
}
