package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("CatalogueController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(CatalogueController.class)
public class CatalogueControllerTest {
    private @MockBean DocumentRepository documentRepository;
    private @MockBean CatalogueService catalogueService;
    private @MockBean(name="permission") PermissionService permissionService;

    @Autowired private MockMvc mvc;
    private CatalogueController controller;

    private final String file = "955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052";

    private void givenCataloguesRetrieveAll() {
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").build();

        given(catalogueService.retrieveAll()).willReturn(Arrays.asList(a, b, c));
    }

    private void givenCatalogue() {
        val catalogue = Catalogue.builder()
            .id("eidc")
            .title("EIDC")
            .url("url")
            .vocabularyPicker(Arrays.asList(
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
                    public boolean usedInCatalogue(String catalogueId) {
                        return true;
                    }
                }
            ))
            .build();
        given(catalogueService.retrieve("eidc"))
            .willReturn(catalogue);
    }

    private void givenUserCanView() {
        given(permissionService.toAccess(any(CatalogueUser.class), eq(file), eq("VIEW")))
            .willReturn(true);
    }

    @SneakyThrows
    private void givenMetadataDocument() {
        val document = new GeminiDocument();
        document.setId(file);
        document.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file))
            .willReturn(document);
    }

    @BeforeEach
    void setup() {
        controller = new CatalogueController(documentRepository, catalogueService);
    }

    @Test
    @SneakyThrows
    void getCatalogue() {
        //given
        givenCatalogue();

        //when
        mvc.perform(get("/catalogues/eidc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json("{\"id\":\"eidc\",\"vocabularies\":[{\"name\": \"ASSIST Topics\",\"id\":\"assist-topics\"}]}"));

        //then
    }

    @Test
    @SneakyThrows
    void getCatalogueForFile() {
        //given
        givenUserCanView();
        givenMetadataDocument();

        //when
        mvc.perform(
            get("/documents/{file}/catalogue", file)
                .accept(APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().string("{\"id\":\"955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052\",\"value\":\"eidc\"}"));
    }

    @Test
    void getAllCatalogues() throws Exception {
        //given
        givenCataloguesRetrieveAll();

        //when
        mvc.perform(
            get("/catalogues")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void getCataloguesMinusB() {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").build();

        given(catalogueService.retrieveAll()).willReturn(Arrays.asList(a, b, c));
        given(catalogueService.retrieve("b")).willReturn(b);

        //when
        List<Catalogue> actual = controller.catalogues("b", null).getBody();

        //then
        verify(catalogueService).retrieveAll();
        verify(catalogueService).retrieve("b");
        assertThat("should be list of catalogues", actual.contains(a));
        assertThat("should be list of catalogues", actual.contains(c));
    }

    @Test
    public void getCataloguesWithUnknownCatalogue() {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").build();

        given(catalogueService.retrieveAll()).willReturn(Arrays.asList(a, b, c));
        given(catalogueService.retrieve("x")).willThrow(CatalogueException.class);

        //when
        List<Catalogue> actual = controller.catalogues("x", null).getBody();

        //then
        verify(catalogueService).retrieveAll();
        verify(catalogueService).retrieve("x");
        assertThat("should be list of catalogues", actual.contains(a));
        assertThat("should be list of catalogues", actual.contains(b));
        assertThat("should be list of catalogues", actual.contains(c));
    }

    @Test
    public void getCataloguesForIdentifier() throws Exception {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").build();

        MetadataDocument document = new GeminiDocument()
            .setMetadata(MetadataInfo.builder().catalogue("a").build());

        given(catalogueService.retrieveAll()).willReturn(Arrays.asList(a, b, c));
        given(documentRepository.read("identifier")).willReturn(document);
        given(catalogueService.retrieve("a")).willReturn(a);

        //when
        List<Catalogue> actual = controller.catalogues(null, "identifier").getBody();

        //then
        verify(catalogueService).retrieveAll();
        verify(documentRepository).read("identifier");
        verify(catalogueService).retrieve("a");
        assertThat("should be list of catalogues", actual.contains(b));
        assertThat("should be list of catalogues", actual.contains(c));
    }

    @Test
    public void getCataloguesForUnknownIdentifier() throws Exception {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").build();

        given(catalogueService.retrieveAll()).willReturn(Arrays.asList(a, b, c));
        given(documentRepository.read("unknown")).willThrow(DocumentRepositoryException.class);

        //when
        List<Catalogue> actual = controller.catalogues(null, "unknown").getBody();

        //then
        verify(catalogueService).retrieveAll();
        verify(documentRepository).read("unknown");
        assertThat("should be list of catalogues", actual.contains(a));
        assertThat("should be list of catalogues", actual.contains(b));
        assertThat("should be list of catalogues", actual.contains(c));
    }

    @Test
    public void getCurrentCatalogue() throws Exception {
        //Given
        String file = "123-456-789";
        MetadataDocument document = new GeminiDocument()
            .setId(file)
            .setMetadata(
                MetadataInfo.builder()
                .catalogue("eidc")
                .build()
        );
        given(documentRepository.read(file)).willReturn(document);

        //When
        controller.currentCatalogue(CatalogueUser.PUBLIC_USER, file);

        //Then
        verify(documentRepository).read(file);
    }

    @Test
    public void getUnknownFile() {
        Assertions.assertThrows(DocumentRepositoryException.class, () -> {
            //Given
            String file = "123-456-789";
            given(documentRepository.read(file)).willThrow(
                    new DocumentRepositoryException("Test", new Exception())
            );

            //When
            controller.currentCatalogue(CatalogueUser.PUBLIC_USER, file);

            //Then
            fail("Expected DocumentRepositoryException");
        });
    }

    @Test
    public void updateCatalogue() throws Exception {
        //Given
        String file = "123-456-789";
        CatalogueResource catalogueResource = new CatalogueResource("1", "eidc");

        MetadataDocument document = new GeminiDocument()
            .setId(file)
            .setMetadata(
                MetadataInfo.builder()
                .catalogue("eidc")
                .build()
        );
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(
            CatalogueUser.PUBLIC_USER,
            document,
            file,
            "Catalogues of 123-456-789 changed."
        )).willReturn(document);

        //When
        controller.updateCatalogue(CatalogueUser.PUBLIC_USER, file, catalogueResource);

        //Then
        verify(documentRepository).read(file);
        verify(documentRepository).save(CatalogueUser.PUBLIC_USER, document, file, "Catalogues of 123-456-789 changed.");
    }

}
