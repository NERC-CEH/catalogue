package uk.ac.ceh.gateway.catalogue.wellknown;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("WellKnownController")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class WellKnownControllerTest extends AbstractMvcTest {

    @SneakyThrows
    @Test
    @DisplayName("GET /.well-known/void returns VoID description as Turtle")
    void getVoidDescription() {
        mvc.perform(get("/.well-known/void"))
            .andExpectAll(
                status().isOk(),
                content().contentType("text/turtle"),
                content().string(containsString("void:sparqlEndpoint")),
                content().string(containsString("void:Dataset"))
            );
    }
}
