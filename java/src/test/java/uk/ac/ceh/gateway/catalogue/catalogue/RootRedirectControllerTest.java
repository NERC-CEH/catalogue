package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.config.CatalogueServiceConfig;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("RootRedirectController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class,
    CatalogueServiceConfig.class
})

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class RootRedirectControllerTest extends AbstractMvcTest {

    @Test
    @SneakyThrows
    void redirectToEidc() {
        //when
        mvc.perform(get("https://catalogue.ceh.ac.uk/").secure(true))
            .andExpect(redirectedUrl("https://catalogue.ceh.ac.uk/eidc/documents"));
    }

    @Test
    @DisplayName("upgrades a plain-HTTP request on a real host to HTTPS (dri-one #71)")
    @SneakyThrows
    void redirectUpgradesRealHostToHttps() {
        //when a proxy has terminated TLS and forwarded plain HTTP
        mvc.perform(get("http://catalogue.ceh.ac.uk/"))
           .andExpect(redirectedUrl("https://catalogue.ceh.ac.uk/eidc/documents"));
    }

    @Test
    @DisplayName("preserves HTTP for local development so localhost is not forced to HTTPS")
    @SneakyThrows
    void redirectPreservesHttpForLocalhost() {
        //when
        mvc.perform(get("http://localhost:8080/"))
           .andExpect(redirectedUrl("http://localhost:8080/eidc/documents"));
    }
}
