package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.RemoteSolrException;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.core.env.Environment;
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
import uk.ac.ceh.gateway.catalogue.search.InvalidFacetException;
import uk.ac.ceh.gateway.catalogue.search.InvalidSortFieldException;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreementException;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadException;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@ControllerAdvice
public class ExceptionControllerHandler extends ResponseEntityExceptionHandler {
    // "Error from server at http://solr:8983/solr/documents/select: org.apache.solr...Exception: "
    private static final Pattern SOLR_REMOTE_ERROR_PREFIX =
        Pattern.compile("^Error from server at \\S+:\\s*(?:[\\w.]*Exception:\\s*)?");

    private final Environment env;

    public ExceptionControllerHandler(Environment env) {
        this.env = env;
    }

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
        String includeStackTrace = env.getProperty("server.error.include-stacktrace");
        boolean showStackTrace = "always".equalsIgnoreCase(includeStackTrace);

        if (ex instanceof PermissionDeniedException) {
            logger.warn("Permission denied: " + ex.getMessage());
        } else if (NOT_FOUND.equals(statusCode)) {
            logger.warn(message);
        } else if (PRECONDITION_REQUIRED.equals(statusCode)) {
            logger.warn(message);
        } else if (BAD_REQUEST.equals(statusCode)) {
            // A malformed request is the caller's fault, not a server failure. Logging it at ERROR
            // meant a crawler walking stale sort/facet links filled the log with false alarms.
            logger.warn(message);
        } else {
            if (showStackTrace) {
                logger.error(message, ex);
            } else {
                logger.error(message);
            }
        }
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

    @ExceptionHandler(InvalidFacetException.class)
    public ResponseEntity<Object> handleInvalidFacetException(InvalidFacetException ex, HttpServletRequest request) {
        String qs = request.getQueryString();
        String fullUrl = request.getRequestURL() + (qs != null ? "?" + qs : "");
        String[] parts = request.getServletPath().split("/");
        String catalogue = parts.length >= 3 ? parts[1] : "all";
        log.warn("Invalid facet in catalogue [{}] for URL [{}]: {}", catalogue, fullUrl, ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    public ResponseEntity<Object> handleInvalidSortFieldException(InvalidSortFieldException ex) {
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

    /**
     * Solr answers a malformed query - an unknown sort field, an unparseable sort spec - with its own
     * 4xx, which SolrJ surfaces as {@link RemoteSolrException}. That extends {@link RuntimeException},
     * not {@link SolrServerException}, so before dri-one #314 it matched no handler at all and every
     * such request became a 500 with a full stack trace in the application log. Map the caller's fault
     * back to a 400 and keep 500 for a Solr that genuinely failed.
     */
    @ExceptionHandler(RemoteSolrException.class)
    public ResponseEntity<Object> handleRemoteSolrException(RemoteSolrException ex) {
        if (ex.code() >= 400 && ex.code() < 500) {
            log.warn("Solr rejected the query: {}", ex.getMessage());
            return handleExceptionInternal(ex, "Invalid search query: " + solrErrorDetail(ex), BAD_REQUEST);
        }
        return handleExceptionInternal(ex, "Solr did not respond as expected", INTERNAL_SERVER_ERROR);
    }

    /**
     * SolrJ prefixes the remote message with the Solr base URL and the server-side exception class.
     * Neither belongs in a response body on a public endpoint, so strip them and return just Solr's
     * own explanation; the untrimmed message is still logged above.
     */
    private static String solrErrorDetail(RemoteSolrException ex) {
        String message = ex.getMessage();
        if (message == null) {
            return "the search backend rejected this request";
        }
        return SOLR_REMOTE_ERROR_PREFIX.matcher(message).replaceFirst("");
    }

    @SuppressWarnings("SpringMVCViewInspection")
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException() {
        CatalogueUser user = (CatalogueUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assert user != null;
        boolean isPublic = user.isPublic();
        return new ModelAndView("html/access-denied", "isPublic", isPublic);
    }

    @ExceptionHandler({DocumentIndexingException.class})
    public ResponseEntity<Object> handleIndexingExceptions(Exception ex) {
        return handleExceptionInternal(ex, ex.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceIdentifierExistsException.class)
    public ResponseEntity<Object> handleResourceIdentifierAlreadyExists(ResourceIdentifierExistsException ex) {
        return handleExceptionInternal(ex, ex.getMessage(), CONFLICT);
    }

    @ExceptionHandler(MetadataConflictException.class)
    public ResponseEntity<MetadataDocument> handleMetadataConflict(MetadataConflictException ex) {
        // 409 with the submitted-but-unsaved document so the caller can preserve the user's edits.
        //
        // The content type is pinned rather than negotiated. When an @ExceptionHandler returns a body no
        // converter can write for the request's Accept, Spring abandons the handler and rethrows the
        // ORIGINAL exception - the caller gets a 500 and loses the echoed submission, which is the one
        // thing this response exists to protect. Accept: */* (curl's default, and what docs/api.md's
        // script sends) hits exactly that case. Setting a concrete type makes Spring write with that
        // converter directly and skip negotiation, so the 409 contract holds for every client.
        log.warn("Metadata save conflict: {}", ex.getMessage());
        return ResponseEntity.status(CONFLICT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ex.getSubmittedDocument());
    }

    @ExceptionHandler(MetadataPreconditionRequiredException.class)
    public ResponseEntity<Object> handleMetadataPreconditionRequired(MetadataPreconditionRequiredException ex) {
        return handleExceptionInternal(ex, ex.getMessage(), HttpStatus.PRECONDITION_REQUIRED);
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
