package uk.ac.ceh.gateway.catalogue.catalogue;

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
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"test"})
@DisplayName("CatalogueController")
@Import({
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class,
    CatalogueModelAssembler.class
})
@WebMvcTest(CatalogueController.class)
public class CatalogueControllerTest {
    private @MockBean DocumentRepository documentRepository;
    private @MockBean CatalogueService catalogueService;

    @Autowired private MockMvc mvc;

    private void givenCataloguesRetrieveAll() {
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").contactUrl("").logo("eidc.png").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").contactUrl("").logo("eidc.png").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").contactUrl("").logo("eidc.png").build();

        given(catalogueService.retrieveAll()).willReturn(List.of(a, b, c));
    }

    private void givenCatalogue() {
        val catalogue = Catalogue.builder()
            .id("eidc")
            .title("EIDC")
            .url("url")
            .contactUrl("contact")
            .logo("eidc.png")
            .vocabularies(List.of(
                new KeywordVocabulary() {
                    @Override
                    public void retrieve() {}

                    @Override
                    public String getName() {
                        return "ASSIST Topics";
                    }

                    @Override
                    public String getId() {
                        return "assist-topics";
                    }

                    @Override
                    public String getGraph() {
                        return "urn:x-evn-master:CEHMD";
                    }

                    @Override
                    public boolean usedInCatalogue(String catalogueId) {
                        return true;
                    }
                }
            ))
            .build();
        given(catalogueService.retrieve("eidc"))
            .willReturn(catalogue);
    }

    private void givenUnknownCatalogue() {
        given(catalogueService.retrieve("unknown"))
            .willThrow(new CatalogueException("test"));
    }

    @Test
    @SneakyThrows
    void getCatalogue() {
        //given
        givenCatalogue();
        val expectedResponse = """
            {
                "id":"eidc",
                "title":"EIDC",
                "vocabularies":[
                    {
                        "name":"ASSIST Topics",
                        "id":"assist-topics"
                    }
                ]
            }
            """;

        //when
        mvc.perform(get("/catalogues/eidc")
                .accept(HAL_JSON)
                .header("Forwarded", "proto=https;host=catalogue")
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(HAL_JSON))
            .andExpect(content().json(expectedResponse));

        //then
    }

    @Test
    @SneakyThrows
    void getUnknownCatalogue() {
        //given
        givenUnknownCatalogue();


        //when
        mvc.perform(get("/catalogues/unknown")
                .accept(HAL_JSON)
                .header("Forwarded", "proto=https;host=catalogue")
            )
            .andExpect(status().isNotFound());

        //then
    }

    @Test
    void getAllCatalogues() throws Exception {
        //given
        givenCataloguesRetrieveAll();
        val expectedResponse = """
            [
                {"id": "a", "title": "a"},
                {"id": "b", "title": "b"},
                {"id": "c", "title": "c"}
            ]
            """;

        //when
        mvc.perform(
            get("/catalogues")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));
    }

    @Test
    @SneakyThrows
    public void getCataloguesMinusB() {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").contactUrl("").logo("eidc.png").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").contactUrl("").logo("eidc.png").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").contactUrl("").logo("eidc.png").build();

        given(catalogueService.retrieveAll()).willReturn(List.of(a, b, c));
        given(catalogueService.retrieve("b")).willReturn(b);

        val expectedResponse = """
            [
                {"id": "a", "title": "a"},
                {"id": "c", "title": "c"}
            ]
            """;

        //when
        mvc.perform(
                get("/catalogues")
                    .queryParam("catalogue", "b")
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));
    }

    @SneakyThrows
    @Test
    public void getCataloguesWithUnknownCatalogue() {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").contactUrl("").logo("eidc.png").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").contactUrl("").logo("eidc.png").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").contactUrl("").logo("eidc.png").build();

        given(catalogueService.retrieveAll()).willReturn(List.of(a, b, c));
        given(catalogueService.retrieve("x")).willThrow(CatalogueException.class);

        val expectedResponse = """
            [
                {"id": "a", "title": "a"},
                {"id": "b", "title": "b"},
                {"id": "c", "title": "c"}
            ]
            """;

        //when
        mvc.perform(
                get("/catalogues")
                    .queryParam("catalogue", "x")
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));

        //then
    }

    @SneakyThrows
    @Test
    public void getCataloguesForIdentifier() {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").contactUrl("").logo("eidc.png").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").contactUrl("").logo("eidc.png").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").contactUrl("").logo("eidc.png").build();

        val document = new GeminiDocument()
            .setMetadata(MetadataInfo.builder().catalogue("a").build());

        given(catalogueService.retrieveAll()).willReturn(List.of(a, b, c));
        given(documentRepository.read("identifier")).willReturn(document);
        given(catalogueService.retrieve("a")).willReturn(a);

        val expectedResponse = """
            [
                {"id": "b", "title": "b"},
                {"id": "c", "title": "c"}
            ]
            """;

        //when
        mvc.perform(
                get("/catalogues")
                    .queryParam("identifier", "identifier")
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));

        //then
    }

    @Test
    public void getCataloguesForUnknownIdentifier() throws Exception {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").contactUrl("").logo("eidc.png").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").contactUrl("").logo("eidc.png").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").contactUrl("").logo("eidc.png").build();

        given(catalogueService.retrieveAll()).willReturn(List.of(a, b, c));
        given(documentRepository.read("unknown")).willThrow(DocumentRepositoryException.class);

        val expectedResponse = """
            [
                {"id": "a", "title": "a"},
                {"id": "b", "title": "b"},
                {"id": "c", "title": "c"}
            ]
            """;

        //when
        mvc.perform(
                get("/catalogues")
                    .queryParam("identifier", "unknown")
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));

        //then
    }
}
