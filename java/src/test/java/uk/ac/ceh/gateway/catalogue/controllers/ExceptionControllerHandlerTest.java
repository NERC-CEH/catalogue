package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.ErrorResponse;
import uk.ac.ceh.gateway.catalogue.model.ExternalResourceFailureException;
import uk.ac.ceh.gateway.catalogue.model.MetadataConflictException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataPreconditionRequiredException;
import uk.ac.ceh.gateway.catalogue.model.MojibakeTextException;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import org.apache.solr.client.solrj.RemoteSolrException;
import uk.ac.ceh.gateway.catalogue.search.InvalidFacetException;
import uk.ac.ceh.gateway.catalogue.search.InvalidSortFieldException;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("ExceptionController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})

public @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ExceptionControllerHandlerTest {
    private ExceptionControllerHandler controller;

    @MockitoBean private DocumentRepository repo;
    @MockitoBean private DocumentIdentifierService identifierService;
    @MockitoBean private DataciteService dataciteService;
    @MockitoBean(name="permission") private PermissionService permissionService;

    @Autowired
    private Environment env;

    @BeforeEach
    public void setup() {
        this.controller = new ExceptionControllerHandler(env);
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
        assert res != null;
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

        //When
        val response = controller.handleURISyntaxException();

        //Then
        assertResponseImageExists(response);
    }

    @Test
    public void checkThatTransparentExceptionReturnsImage() {
        //Given

        //When
        val response = controller.handleTransparentProxyException();

        //Then
        assertResponseImageExists(response);
    }

    @Test
    public void checkThatMissingLegendExceptionReturnsImage() {
        //Given

        //When
        val response = controller.handleLegendGraphicMissingException();

        //Then
        assertResponseImageExists(response);
    }

    @Test
    public void checkThatUpstreamInvalidMediaTypeExceptionReturnsImage() {
        //Given

        //When
        val response = controller.handleUpstreamInvalidMediaTypeException();

        //Then
        assertResponseImageExists(response);
    }

    @Test
    public void checkThatInvalidFacetExceptionReturnsBadRequest() {
        //Given
        String mess = "Unknown facet field(s): badField";
        InvalidFacetException ex = new InvalidFacetException(mess);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/eidc/documents");
        request.setQueryString("facet=badField:x");

        //When
        ResponseEntity<Object> response = controller.handleInvalidFacetException(ex, request);

        //Then
        assertThat(response.getStatusCode(), equalTo(BAD_REQUEST));
        assert response.getBody() != null;
        assertThat(((ErrorResponse) response.getBody()).getMessage(), equalTo(mess));
    }

    @Test
    public void conflictExceptionMapsTo409WithSubmittedDocumentBody() {
        //Given
        MetadataDocument submitted = new GeminiDocument();
        MetadataConflictException ex = new MetadataConflictException("stale", submitted);

        //When
        ResponseEntity<MetadataDocument> response = controller.handleMetadataConflict(ex);

        //Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CONFLICT));
        assertThat(response.getBody(), sameInstance(submitted));
    }

    @Test
    public void preconditionRequiredMapsTo428() {
        //Given
        MetadataPreconditionRequiredException ex = new MetadataPreconditionRequiredException("need If-Match");

        //When
        ResponseEntity<Object> response = controller.handleMetadataPreconditionRequired(ex);

        //Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.PRECONDITION_REQUIRED));
    }

    private void assertResponseImageExists(ResponseEntity<Object> response) {
        HttpHeaders headers = response.getHeaders();
        ClassPathResource body = (ClassPathResource)response.getBody();

        //Then
        assert body != null;
        assertTrue(body.exists());
        assertThat(headers.getContentType(), equalTo(MediaType.IMAGE_PNG));
    }

    @Test
    public void checkThatInvalidSortFieldExceptionReturnsBadRequest() {
        //Given
        String mess = "Unknown sort field: jam. Allowed values: description, publicationDate";
        InvalidSortFieldException ex = new InvalidSortFieldException(mess);

        //When
        ResponseEntity<Object> response = controller.handleInvalidSortFieldException(ex);

        //Then
        assertThat(response.getStatusCode(), equalTo(BAD_REQUEST));
        assert response.getBody() != null;
        assertThat(((ErrorResponse) response.getBody()).getMessage(), equalTo(mess));
    }

    @Test
    @DisplayName("A save containing double-encoded (mojibake) text becomes a 400 (dri-one #328)")
    public void checkThatMojibakeTextExceptionReturnsBadRequest() {
        //Given
        String mess = "Document tulips contains text matching the double-encoding (mojibake) signature";
        MojibakeTextException ex = new MojibakeTextException(mess);

        //When
        ResponseEntity<Object> response = controller.handleMojibakeText(ex);

        //Then
        assertThat(response.getStatusCode(), equalTo(BAD_REQUEST));
        assert response.getBody() != null;
        assertThat(((ErrorResponse) response.getBody()).getMessage(), equalTo(mess));
    }

    @Test
    @DisplayName("A Solr 4xx becomes a 400, not the 500 it used to fall through to (dri-one #314)")
    public void solrClientErrorReturnsBadRequest() {
        //Given
        RemoteSolrException ex = new RemoteSolrException(
            "http://solr:8983/solr/documents/select",
            400,
            "org.apache.solr.common.SolrException: sort param field can't be found: lastupdate",
            null
        );

        //When
        ResponseEntity<Object> response = controller.handleRemoteSolrException(ex);

        //Then
        assertThat(response.getStatusCode(), equalTo(BAD_REQUEST));
        assert response.getBody() != null;
        assertThat(
            ((ErrorResponse) response.getBody()).getMessage(),
            equalTo("Invalid search query: sort param field can't be found: lastupdate")
        );
    }

    @Test
    @DisplayName("The 400 body names neither the internal Solr URL nor the server-side exception class")
    public void solrClientErrorDoesNotLeakSolrInternals() {
        //Given
        RemoteSolrException ex = new RemoteSolrException(
            "http://solr:8983/solr/documents/select",
            400,
            "org.apache.solr.common.SolrException: Can't determine a Sort Order (asc or desc)",
            null
        );

        //When
        ResponseEntity<Object> response = controller.handleRemoteSolrException(ex);

        //Then
        assert response.getBody() != null;
        String message = ((ErrorResponse) response.getBody()).getMessage();
        assertThat(message, not(containsString("solr:8983")));
        assertThat(message, not(containsString("org.apache.solr")));
        assertThat(message, containsString("Can't determine a Sort Order"));
    }

    @Test
    @DisplayName("A Solr server-side failure still returns 500")
    public void solrServerErrorReturnsInternalServerError() {
        //Given
        RemoteSolrException ex = new RemoteSolrException(
            "http://solr:8983/solr/documents/select", 500, "boom", null
        );

        //When
        ResponseEntity<Object> response = controller.handleRemoteSolrException(ex);

        //Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
        assert response.getBody() != null;
        assertThat(
            ((ErrorResponse) response.getBody()).getMessage(),
            equalTo("Solr did not respond as expected")
        );
    }
}
