package uk.ac.ceh.gateway.catalogue.relationships;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The JSON the editor's record picker reads ({@code RelationshipView.searchQuery}). The
 * application's mapper leaves empty lists out, so a rule searching the record's own catalogue
 * arrives with no {@code catalogues} at all, and the picker has to treat that as empty.
 */
@WithMockCatalogueUser
@ActiveProfiles({"server-eidc", "test", "search-basic"})
@DisplayName("RelationshipRulesController over HTTP")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class RelationshipRulesControllerMvcTest extends AbstractMvcTest {

    @Test
    @SneakyThrows
    void rulesForADataset() {
        mvc.perform(get("/relationships/rules").queryParam("type", "dataset"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$['" + RelationshipRules.UTILISES + "'].resourceTypes[0]").value("Monitoring facility"))
            .andExpect(jsonPath("$['" + RelationshipRules.UTILISES + "'].catalogues[1]").value("ukceh"))
            .andExpect(jsonPath("$['" + RelationshipRules.REPLACES + "'].resourceTypes[0]").value("Dataset"))
            // The contract RelationshipView relies on: own-catalogue rules arrive without the key
            .andExpect(jsonPath("$['" + RelationshipRules.REPLACES + "'].catalogues").doesNotExist())
            .andExpect(jsonPath("$['http://purl.org/dc/terms/relation']").doesNotExist());
    }
}
