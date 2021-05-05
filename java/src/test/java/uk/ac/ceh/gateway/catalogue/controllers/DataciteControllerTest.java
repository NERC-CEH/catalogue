package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.DATACITE_SHORT;
import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.DATACITE_XML_VALUE;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.EIDC_PUBLISHER_USERNAME;

@ActiveProfiles("test")
@DisplayName("DataciteController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(DataciteController.class)
class DataciteControllerTest {
    @MockBean private DocumentRepository documentRepository;
    @MockBean private DocumentIdentifierService identifierService;
    @MockBean private DataciteService dataciteService;

    @Autowired private MockMvc mockMvc;

    private final String file = "1234";

    @SneakyThrows
    private void givenDocumentRepository() {
        val gemini = new GeminiDocument();
        gemini.setResourceIdentifiers(new ArrayList<>());
        given(documentRepository.read(file))
            .willReturn(gemini);
    }

    private void givenDataciteService() {
        given(dataciteService.getDatacitationRequest(any(GeminiDocument.class)))
            .willReturn("data citation request");
    }

    private void givenGenerateDoi() {
        given(dataciteService.generateDoi(any(GeminiDocument.class)))
            .willReturn(ResourceIdentifier.builder().code(file).codeSpace("doi").build());
    }

    @Test
    void getDataciteXml() throws Exception {
        //given
        givenDocumentRepository();
        givenDataciteService();

        //when
        mockMvc.perform(
            get("/documents/{file}/datacite?format=datacite", file)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(DATACITE_XML_VALUE))
            .andExpect(content().string("data citation request"));
    }

    @Test
    void getDataciteXmlNoAccept() throws Exception {
        //given
        givenDocumentRepository();
        givenDataciteService();

        //when
        mockMvc.perform(
            get("/documents/{file}/datacite.xml", file)
        )
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("forward:/documents/" + file + "/datacite?format=" + DATACITE_SHORT))
            .andExpect(content().contentTypeCompatibleWith(DATACITE_XML_VALUE))
            .andExpect(content().string("data citation request"));
    }

    @Test
    void mintDoi() throws Exception {
        //given
        givenDocumentRepository();
        givenGenerateDoi();
        given(identifierService.generateUri(file)).willReturn("http://example.com/1234");

        //when
        mockMvc.perform(
            post("/documents/{file}/datacite", file)
            .header("remote-user", EIDC_PUBLISHER_USERNAME)
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "http://example.com/1234"));
    }
}
