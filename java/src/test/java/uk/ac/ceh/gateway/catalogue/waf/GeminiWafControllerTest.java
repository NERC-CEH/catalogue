package uk.ac.ceh.gateway.catalogue.waf;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.GEMINI_XML_SHORT;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("GeminiWafController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class GeminiWafControllerTest extends AbstractMvcTest {
    @MockitoBean private GeminiWafService geminiWafService;

    @Test
    @SneakyThrows
    void checkThatXmlExtensionIsAppendedToGeminiMetadataRecords() {
        //Given
        List<String> files = Arrays.asList("test1.xml", "test2.xml");
        given(geminiWafService.getWafFiles()).willReturn(files);

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
