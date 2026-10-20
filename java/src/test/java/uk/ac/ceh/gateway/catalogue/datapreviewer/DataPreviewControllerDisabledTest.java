package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

@ActiveProfiles({"test", "server-eidc", "search-basic", "upload-hubbub"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = "catalogue.data-preview.enabled=false")
class DataPreviewControllerDisabledTest extends AbstractMvcTest {

    @Test
    void notRegisteredWhenDisabled() throws Exception {
        mvc.perform(get("/documents/abc/data-preview")).andExpect(status().isNotFound());
    }
}
