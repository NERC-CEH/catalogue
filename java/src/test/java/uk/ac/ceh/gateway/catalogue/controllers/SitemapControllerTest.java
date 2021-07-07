package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
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
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("SitemapController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=SitemapController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class SitemapControllerTest {
    @MockBean private CatalogueService catalogueService;
    @MockBean private DocumentIdentifierService documentIdentifierService;
    @MockBean private MetadataListingService metadataListingService;

    @Autowired
    private MockMvc mvc;

    private final String catalogueKey = "eidc";

    private void givenGeneratedUrls() {
        given(documentIdentifierService.generateUri(anyString()))
            .willReturn("A","B", "C");
    }

    private void givenPublicDocuments() {
        given(metadataListingService.getPublicDocumentsOfCatalogue(catalogueKey))
            .willReturn(Arrays.asList(
                "doc0",
                "doc1",
                "doc2"
            ));
    }

    private void givenCatalogue() {
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(Catalogue.builder().id("A").title("A").url("").build());
    }

    private void givenAllCatalogues() {
        given(catalogueService.retrieveAll())
            .willReturn(Arrays.asList(
                Catalogue.builder().id("A").title("A").url("").build(),
                Catalogue.builder().id("B").title("B").url("").build()
            ));
    }

    private void givenBaseUri() {
        given(documentIdentifierService.getBaseUri())
            .willReturn("https://example.com");
    }

    @Test
    @SneakyThrows
    void getRobots() {
        //given
        givenAllCatalogues();
        givenBaseUri();

        //when
        mvc.perform(
            get("/robots.txt")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE))
            .andExpect(view().name("sitemap/robots.txt"))
            .andExpect(model().attributeExists("baseUri", "catalogues"));
    }

    @Test
    @SneakyThrows
    void getSitemap() {
        //given
        givenCatalogue();
        givenPublicDocuments();
        givenGeneratedUrls();

        //when
        mvc.perform(
            get("/{catalogue}/sitemap.txt", catalogueKey)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE))
            .andExpect(view().name("sitemap/sitemap.txt"))
            .andExpect(model().attributeExists("urls"));
    }
}
