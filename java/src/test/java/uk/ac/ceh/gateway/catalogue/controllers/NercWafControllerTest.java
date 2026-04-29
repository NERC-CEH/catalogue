package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.GEMINI_XML_SHORT;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("NercWafController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties="spring.freemarker.template-loader-path=file:../templates")
class NercWafControllerTest extends AbstractMvcTest {

    @MockitoBean private DataRepository<CatalogueUser> repo;
    @MockitoBean private MetadataListingService listing;

    @SneakyThrows
    private void givenLatestRevision() {
        val dataRevision = mock(DataRevision.class);
        given(repo.getLatestRevision())
            .willReturn(dataRevision);
        given(dataRevision.getRevisionID())
            .willReturn("1234");
    }

    @SneakyThrows
    private void givenPublicDocuments() {
        val resourceTypes = List.of(
            "application",
            "dataset",
            "nonGeographicDataset",
            "service",
            "nercSignpost"
        );
        given(listing.getPublicDocuments(eq("1234"), eq(GeminiDocument.class), eq(resourceTypes)))
            .willReturn(List.of("a", "b", "c"));
    }

    @Test
    @SneakyThrows
    void getWaf() {
        //given
        givenLatestRevision();
        givenPublicDocuments();

        //when
        mvc.perform(get("/documents/nerc/waf/"))
            .andExpect(status().isOk())
            .andExpect(view().name("/html/waf"))
            .andExpect(model().attribute("files", List.of("a.xml", "b.xml", "c.xml")));
    }

    @Test
    @SneakyThrows
    void forwardToMetadata() {
        //Given
        String id = "someRandomId";

        //When
        mvc.perform(
                get("/documents/nerc/waf/{id}.xml", id)
            )
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/documents/" + id + "?format=" + GEMINI_XML_SHORT));
    }
}
