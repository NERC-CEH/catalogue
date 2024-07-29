package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueException;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreementException;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadException;

import java.net.URISyntaxException;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@ControllerAdvice
public class ExceptionControllerHandler extends ResponseEntityExceptionHandler {

    @Override
    @SuppressWarnings("NullableProblems")
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex,
        Object body,
        HttpHeaders headers,
        HttpStatusCode statusCode,
        WebRequest request
    ) {
        String message = (body != null) ? body.toString() : statusCode.toString();
        logger.error(message, ex);
        return new ResponseEntity<>(new ErrorResponse(message), headers, statusCode);
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpStatus status) {
        //noinspection ConstantConditions
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, null);
    }

    @ExceptionHandler({HttpClientErrorException.BadRequest.class})
    public ResponseEntity<Object> handleBadRequestException(HttpClientErrorException.BadRequest ex) {
        return handleExceptionInternal(ex, ex.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler({HttpClientErrorException.NotFound.class})
    public ResponseEntity<Object> handleNotFoundException(HttpClientErrorException.NotFound ex) {
        return handleExceptionInternal(ex, ex.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler({HttpClientErrorException.Conflict.class})
    public ResponseEntity<Object> handleConflictException(HttpClientErrorException.Conflict ex) {
        return handleExceptionInternal(ex, ex.getMessage(), CONFLICT);
    }

    @ExceptionHandler(MapServerException.class)
    public ResponseEntity<String> handleMapServerException(MapServerException ex) {
        return ex.asResponseEntity();
    }

    @ExceptionHandler(ServiceAgreementException.class)
    public ResponseEntity<Object> handleServiceAgreementException(ServiceAgreementException ex) {
        return handleExceptionInternal(ex, ex.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(UploadException.class)
    public ResponseEntity<Object> handleUploadException(UploadException ex) {
        return handleExceptionInternal(ex, ex.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler({
        GitFileNotFoundException.class,
        ResourceNotFoundException.class,
        CatalogueException.class
    })
    public ResponseEntity<Object> handleNotFoundExceptions(Exception ex) {
        return handleExceptionInternal(ex, ex.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(PostProcessingException.class)
    public ResponseEntity<Object> handlePostProcessingException(Exception ex) {
        return handleExceptionInternal(ex, ex.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataciteException.class)
    public ResponseEntity<Object> handleDataciteException(Exception ex) {
        return handleExceptionInternal(ex, ex.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SolrServerException.class)
    public ResponseEntity<Object> handleSolrServerException(Exception ex) {
        return handleExceptionInternal(ex, "Solr did not respond as expected", INTERNAL_SERVER_ERROR);
    }

    @SuppressWarnings("SpringMVCViewInspection")
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException() {
        CatalogueUser user = (CatalogueUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isPublic = user.isPublic();
        return new ModelAndView("html/access-denied", "isPublic", isPublic);
    }

    @ExceptionHandler({DocumentIndexingException.class})
    public ResponseEntity<Object> handleIndexingExceptions(Exception ex) {
        return handleExceptionInternal(ex, ex.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExternalResourceFailureException.class)
    public ResponseEntity<Object> handleExternalResourceFailureException(ExternalResourceFailureException ex) {
        return handleExceptionInternal(ex, ex.getMessage(), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(TransparentProxyException.class)
    @ResponseBody
    public ResponseEntity<Object> handleTransparentProxyException() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(
                new ClassPathResource("proxy-failure.png"),
                headers,
                HttpStatus.GATEWAY_TIMEOUT);
    }

    @ExceptionHandler(URISyntaxException.class)
    @ResponseBody
    public ResponseEntity<Object> handleURISyntaxException() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(
                new ClassPathResource("proxy-invalid-resource.png"),
                headers,
                HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(UpstreamInvalidMediaTypeException.class)
    @ResponseBody
    public ResponseEntity<Object> handleUpstreamInvalidMediaTypeException() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(
                new ClassPathResource("proxy-invalid-response.png"),
                headers,
                HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(LegendGraphicMissingException.class)
    @ResponseBody
    public ResponseEntity<Object> handleLegendGraphicMissingException() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(
                new ClassPathResource("legend-not-found.png"),
                headers,
                NOT_FOUND);
    }
}
