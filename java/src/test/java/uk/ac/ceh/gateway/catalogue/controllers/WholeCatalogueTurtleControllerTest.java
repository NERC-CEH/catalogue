package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.DocumentsToTurtleService;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@DisplayName("WholeCatalogueTurtleController")
@Import(DocumentsToTurtleService.class)
@WebMvcTest(
    controllers = WholeCatalogueTurtleController.class,
    properties = "spring.freemarker.template-loader-path=file:../templates"
)
class WholeCatalogueTurtleControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
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
