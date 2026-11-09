package uk.ac.ceh.gateway.catalogue.document;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.model.ResourceIdentifierLookupException;
import uk.ac.ceh.gateway.catalogue.services.ResourceIdentifierLookupService;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("IdController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})

@TestPropertySource(locations="classpath:test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class IdControllerTest extends AbstractMvcTest {
    @MockitoBean
    private ResourceIdentifierLookupService resolver;


    private final String id = "fe26bd48-0f81-4a37-8a28-58427b7e20bd";

    @Test
    @SneakyThrows
    void checkItCanRewriteIdToDocumentWithFileExtension() {
        //When
        mvc.perform(
            get("/id/{id}.xml", id)
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "http://localhost:8080/documents/" + id +".xml"));
    }

    @Test
    @DisplayName("Redirect URL has query string parameters")
    @SneakyThrows
    public void redirectWithQueryString() {
        //When
        mvc.perform(
            get("/id/{id}", id)
            .queryParam("query", "string")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "http://localhost:8080/documents/" + id + "?query=string"));
    }

    @Test
    @DisplayName("Resolve codespace/code redirects to resolved UUID")
    @SneakyThrows
    void resolveCodespaceAndCode() {
        String codespace = "eidc";
        String code = "my-test-ri";
        String combined = codespace + ":" + code;
        String resolvedUuid = "b7567cab-2ecb-41ef-bac3-d37e71924ee2";

        given(resolver.resolveToUuid(combined)).willReturn(Optional.of(resolvedUuid));

        mvc.perform(get("/id/{codespace}/{code}", codespace, code))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string(
                "location",
                "http://localhost:8080/documents/" + resolvedUuid
            ));
    }

    @Test
    @DisplayName("Resolve codespace/code with no match still falls back to the combined id")
    @SneakyThrows
    void resolveCodespaceAndCodeNoMatch() {
        given(resolver.resolveToUuid("eidc:unknown")).willReturn(Optional.empty());

        mvc.perform(get("/id/{codespace}/{code}", "eidc", "unknown"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "http://localhost:8080/documents/eidc:unknown"));
    }

    /**
     * A failed lookup used to look like "no match", redirecting to /documents/{codespace}:{code},
     * which 404s. A URI that exists would be reported as missing whenever Solr was down or
     * reindexing (dri-one #522).
     */
    @Test
    @DisplayName("Resolve codespace/code returns 503, not a redirect, when the lookup fails")
    @SneakyThrows
    void resolveCodespaceAndCodeLookupFailure() {
        given(resolver.resolveToUuid("ecn:T02"))
            .willThrow(new ResourceIdentifierLookupException("unavailable", new RuntimeException("Solr down")));

        mvc.perform(get("/id/{codespace}/{code}", "ecn", "T02"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(header().doesNotExist("location"));
    }
}
