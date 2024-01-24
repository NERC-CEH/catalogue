package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.ADMIN;
import static uk.ac.ceh.gateway.catalogue.controllers.DocumentController.MAINTENANCE_ROLE;

@WithMockCatalogueUser(
    username=ADMIN,
    grantedAuthorities=MAINTENANCE_ROLE
)
@ActiveProfiles("test")
@DisplayName("SparqlController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})
@WebMvcTest(
    controllers=SparqlController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class SparqlControllerTest {
    @MockBean private CatalogueService catalogueService;
    @MockBean private Dataset jenaTdb;

    @Autowired private MockMvc mvc;
    @Autowired private Configuration configuration;

    private final String catalogueKey = "eidc";

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(Catalogue.builder()
            .id(catalogueKey)
            .title("Foo")
            .url("https://example.com")
            .contactUrl("")
            .logo("eidc.png")
            .build());
    }

    @Test
    @SneakyThrows
    void getSparqlPage() {
        //given
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        //when
        mvc.perform(
            get("/maintenance/sparql")
                .header("remote-user", ADMIN)
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML));

        //then
        verifyNoInteractions(jenaTdb);
    }

    @Test
    @SneakyThrows
    void postSparqlQuery() {
        //given
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();
        val query = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10";

        //when
        mvc.perform(
            post("/maintenance/sparql")
                .queryParam("query", query)
                .header("remote-user", ADMIN)
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML));

        //then
        verify(jenaTdb).begin(ReadWrite.READ);
        verify(jenaTdb).end();
    }
}
