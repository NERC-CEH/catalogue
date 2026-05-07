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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.model.ErrorResponse;
import uk.ac.ceh.gateway.catalogue.model.ExternalResourceFailureException;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("ExceptionController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})

public @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties="spring.freemarker.template-loader-path=file:../templates")
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

    private void assertResponseImageExists(ResponseEntity<Object> response) {
        HttpHeaders headers = response.getHeaders();
        ClassPathResource body = (ClassPathResource)response.getBody();

        //Then
        assert body != null;
        assertTrue(body.exists());
        assertThat(headers.getContentType(), equalTo(MediaType.IMAGE_PNG));
    }
}
