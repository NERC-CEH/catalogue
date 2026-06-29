package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockCatalogueUser(username = DevelopmentUserStoreConfig.UPLOADER_USERNAME)
@Slf4j
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})
@DisplayName("CustomErrorView")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CustomErrorViewTest extends AbstractMvcTest {

    @MockitoBean private CatalogueService catalogueService;
    @MockitoBean private ProfileService profileService;
    @Autowired private Configuration configuration;

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("profile", profileService);
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(Catalogue.builder()
                .title("Foo")
                .id("eidc")
                .url("bar")
                .contactUrl("")
                .logo("eidc.png")
                .build()
            );
    }

    @Test
    @SneakyThrows
    @DisplayName("renders a branded 'page not found' page for a 404 instead of the Whitelabel page")
    void rendersBrandedNotFoundPage() {
        //given
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        //when BasicErrorController handles a 404 it must render our branded error view
        mvc.perform(
                get("/error")
                    .accept(MediaType.TEXT_HTML)
                    .requestAttr("jakarta.servlet.error.status_code", 404)
                    .requestAttr("jakarta.servlet.error.request_uri", "/eidc/does-not-exist")
            )
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(view().name("error"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Page not found")))
            .andExpect(content().string(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.containsString("no explicit mapping for /error"))));
    }

    @Test
    @SneakyThrows
    @DisplayName("renders a branded 'something went wrong' page for a 500")
    void rendersBrandedServerErrorPage() {
        //given
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        //when
        mvc.perform(
                get("/error")
                    .accept(MediaType.TEXT_HTML)
                    .requestAttr("jakarta.servlet.error.status_code", 500)
                    .requestAttr("jakarta.servlet.error.request_uri", "/eidc/boom")
            )
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(view().name("error"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Something went wrong")));
    }
}
