package uk.ac.ceh.gateway.catalogue.exports;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("WholeCatalogueTurtleController")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties="spring.freemarker.template-loader-path=file:../templates")
class WholeCatalogueTurtleControllerTest extends AbstractMvcTest {

    @MockitoBean
    private DocumentsToTurtleService documentsToTurtleService;

    private static final String catalogueKey = "eidc";

    @SneakyThrows
    @Test
    void getBigTtl() {
        //Given
        given(documentsToTurtleService.getBigTtl(catalogueKey))
            .willReturn(Optional.of(""));

        //When
        mvc.perform(get("/{catalogueKey}/catalogue.ttl", catalogueKey))
            .andExpectAll(
                status().isOk(),
                content().contentType("text/turtle")
            );
    }

    @SneakyThrows
    @Test
    void getBigTtlUnknownCatalogue() {
        //Given
        given(documentsToTurtleService.getBigTtl(catalogueKey))
            .willReturn(Optional.empty());

        //When
        mvc.perform(get("/{catalogueKey}/catalogue.ttl", catalogueKey))
            .andExpect(status().isNotFound());
    }
}
