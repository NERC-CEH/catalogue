package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;

import java.net.URISyntaxException;

@Slf4j
@ControllerAdvice
public class ExceptionControllerHandler extends ResponseEntityExceptionHandler {

    @Override
    @SuppressWarnings("NullableProblems")
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex,
        Object body,
        HttpHeaders headers,
        HttpStatus status,
        WebRequest request
    ) {
        String message = (body != null) ? body.toString() : status.getReasonPhrase();
        logger.error(message, ex);
        return new ResponseEntity<>(new ErrorResponse(message), headers, status);
    }
    
    private ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpStatus status) {
        return handleExceptionInternal(ex, body, null, status, null);
    }
    
    @ExceptionHandler(MapServerException.class)
    public ResponseEntity<String> handleMapServerException(MapServerException ex) {
        return ex.asResponseEntity();
    }
    
    @ExceptionHandler({
        GitFileNotFoundException.class,
        ResourceNotFoundException.class
    })
    public ResponseEntity<Object> handleNotFoundExceptions(Exception ex) {
        return handleExceptionInternal(ex, ex.getMessage(), HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(PostProcessingException.class)
    public ResponseEntity<Object> handlePostProcessingException(Exception ex) {
        return handleExceptionInternal(ex, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(DataciteException.class)
    public ResponseEntity<Object> handleDataciteException(Exception ex) {
        return handleExceptionInternal(ex, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(SolrServerException.class)
    public ResponseEntity<Object> handleSolrServerException(Exception ex) {
        return handleExceptionInternal(ex, "Solr did not respond as expected", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException() {
        CatalogueUser user = (CatalogueUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isPublic = user.isPublic();
        return new ModelAndView("html/access-denied", "isPublic", isPublic);
    }
    
    @ExceptionHandler({DocumentIndexingException.class})
    public ResponseEntity<Object> handleIndexingExceptions(Exception ex) {
        return handleExceptionInternal(ex, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(ExternalResourceFailureException.class)
    public ResponseEntity<Object> handleExternalResourceFailureException(ExternalResourceFailureException ex) {
        return handleExceptionInternal(ex, ex.getMessage(), HttpStatus.BAD_GATEWAY);
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
