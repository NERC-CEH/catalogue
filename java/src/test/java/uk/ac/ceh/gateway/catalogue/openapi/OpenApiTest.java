package uk.ac.ceh.gateway.catalogue.openapi;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;
import uk.ac.ceh.gateway.catalogue.config.CatalogueServiceConfig;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("OpenAPI documentation endpoint")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class,
    CatalogueServiceConfig.class
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class OpenApiTest extends AbstractMvcTest {

    @Test
    @SneakyThrows
    @DisplayName("GET /v3/api-docs returns OpenAPI 3 JSON")
    void apiDocsReturnsOpenApiJson() {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.openapi").value(containsString("3.")))
            .andExpect(jsonPath("$.info.title").value("EIP Catalogue API"));
    }

    @Test
    @SneakyThrows
    @DisplayName("GET /v3/api-docs describes document endpoints")
    void apiDocsDescribesDocumentEndpoints() {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/documents/{file}/publication']").exists());
    }

    @Test
    @SneakyThrows
    @DisplayName("@ActiveUser injected parameters are not exposed in operation parameter lists")
    void activeUserParametersAreExcluded() {
        // CitationController.getCitationByFormat has @ActiveUser CatalogueUser user plus
        // @PathVariable file and @RequestParam format — only the latter two should appear.
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/documents/{file}/citation'].get.parameters[*].name",
                not(hasItem("user"))));
    }

    @Test
    @SneakyThrows
    @DisplayName("Swagger UI is accessible")
    void swaggerUiIsAccessible() {
        mvc.perform(get("/swagger-ui.html"))
            .andExpect(status().is3xxRedirection());
    }
}
