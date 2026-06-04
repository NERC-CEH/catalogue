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
        mvc.perform(get("/"))
            .andExpect(redirectedUrl("/eidc/documents"));
    }
}
