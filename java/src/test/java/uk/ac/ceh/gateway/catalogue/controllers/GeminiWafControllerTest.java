package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.permission.CrowdPermissionServiceTest;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.GEMINI_XML_SHORT;

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("GeminiWafController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=GeminiWafController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class GeminiWafControllerTest {
    @MockBean private DataRepository<CatalogueUser> repo;
    @MockBean private MetadataListingService listingService;

    @Autowired private MockMvc mvc;

    @Test
    @SneakyThrows
    void checkThatXmlExtensionIsAppendedToGeminiMetadataRecords() {
        //Given
        List<String> files = Arrays.asList("test1", "test2");
        List<String> resourceTypes = new ArrayList<>(Arrays.asList("dataset", "service"));
        given(repo.getLatestRevision()).willReturn(new CrowdPermissionServiceTest.DummyRevision("latest"));
        given(listingService.getPublicDocuments("latest", GeminiDocument.class, resourceTypes))
            .willReturn(files);

        //When
        mvc.perform(
            get("/documents/gemini/waf/")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("/html/waf"))
            .andExpect(model().attribute("files", Arrays.asList("test1.xml", "test2.xml")));
    }

    @Test
    @SneakyThrows
    public void checkThatGettingDocumentForwardsToDocumentsEndpoint() {
        //Given
        String id = "somerandomID";

        //When
        mvc.perform(
            get("/documents/gemini/waf/{id}.xml", id)
        )
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/documents/" + id + "?format=" + GEMINI_XML_SHORT));
    }
}
