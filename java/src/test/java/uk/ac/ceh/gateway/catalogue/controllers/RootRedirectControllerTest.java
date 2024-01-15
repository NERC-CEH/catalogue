package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.config.CatalogueServiceConfig;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@ActiveProfiles({"test", "server:eidc"})
@DisplayName("RootRedirectController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class, CatalogueServiceConfig.class})
@WebMvcTest(RootRedirectController.class)
class RootRedirectControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @SneakyThrows
    void redirectToEidc() {
        //when
        mvc.perform(get("/"))
            .andExpect(redirectedUrl("/eidc/documents"));
    }
}
