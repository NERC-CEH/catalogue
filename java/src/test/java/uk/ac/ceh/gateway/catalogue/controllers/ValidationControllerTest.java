package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
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
import uk.ac.ceh.gateway.catalogue.indexing.ValidationIndexingService;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;
import uk.ac.ceh.gateway.catalogue.validation.ValidationLevel;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.ValidationResult;

import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.ADMIN;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.MAINTENANCE_ROLE;

@WithMockCatalogueUser(
    username=ADMIN,
    grantedAuthorities=MAINTENANCE_ROLE
)
@ActiveProfiles("test")
@DisplayName("ValidationController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=ValidationController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class ValidationControllerTest {
    @MockBean
    private CatalogueService catalogueService;
    @MockBean private ValidationIndexingService<MetadataDocument> validationIndexingService;

    @Autowired
    private MockMvc mockMvc;
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
                .build());
    }

    private void givenResults() {
        val report0 = new ValidationReport("zero");
        report0.addValidationResult("foo", new ValidationResult().reject("rejection message", ValidationLevel.ERROR));
        given(validationIndexingService.getResults())
            .willReturn(Arrays.asList(
                report0,
                new ValidationReport("first"),
                new ValidationReport("second")
            ));
    }

    private void givenFailedResults() {
        given(validationIndexingService.getFailed())
            .willReturn(Arrays.asList(
                "one", "two", "three"
            ));
    }

    @Test
    @SneakyThrows
    void getValidationPage() {
        //given
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();
        givenResults();
        givenFailedResults();

        //when
        mockMvc.perform(
            get("/maintenance/validation")
                .header("remote-user", ADMIN)
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML));
    }
}
