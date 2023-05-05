package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.model.ErrorResponse;
import uk.ac.ceh.gateway.catalogue.model.ExternalResourceFailureException;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.DATACITE_XML_VALUE;

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("ExceptionController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=DataciteController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
public class ExceptionControllerHandlerTest {
    private ExceptionControllerHandler controller;

    @MockBean private DocumentRepository repo;
    @MockBean private DocumentIdentifierService identifierService;
    @MockBean private DataciteService dataciteService;
    @MockBean(name="permission") private PermissionService permissionService;

    @Autowired private MockMvc mvc;

    private final String file = "1234";

    @BeforeEach
    public void setup() {
        this.controller = new ExceptionControllerHandler();
    }

    @Test
    @SneakyThrows
    void handleException() {
        //given
        given(repo.read(file)).willReturn(new CehModel());

        //when
        mvc.perform(
            get("/documents/{file}/datacite", file)
                .accept(DATACITE_XML_VALUE)
        )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(DATACITE_XML_VALUE))
            .andExpect(content().xml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<error>There was no gemini document present with this address</error>"));
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
        assertTrue(body.exists());
        assertThat(headers.getContentType(), equalTo(MediaType.IMAGE_PNG));
    }
}
