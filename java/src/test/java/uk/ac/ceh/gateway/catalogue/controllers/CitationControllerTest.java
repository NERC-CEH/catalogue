package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.StreamUtils;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.citation.Citation;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.citation.CitationService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.*;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.EIDC_PUBLISHER_USERNAME;

@WithMockCatalogueUser
@Slf4j
@ActiveProfiles("test")
@DisplayName("CitationController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=CitationController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class CitationControllerTest {
    @MockBean DocumentRepository documentRepository;
    @MockBean CitationService citationService;
    @MockBean(name="permission") PermissionService permission;

    @Autowired private MockMvc mvc;

    private static final String file = "file";
    private static final String revision = "revision";

    private void givenUserIsPermittedToViewCurrent() {
        given(permission.toAccess(any(CatalogueUser.class), eq(file), eq("VIEW"))).willReturn(true);
    }

    private void givenUserIsPermittedToViewHistoric() {
        given(permission.toAccess(any(CatalogueUser.class), eq(file), eq(revision), eq("VIEW"))).willReturn(true);
    }

    @SneakyThrows
    private void givenCitation(boolean historic) {
        val document = new GeminiDocument();
        document.setId("gemini-0");
        if (historic) {
            given(documentRepository.read(file, revision))
                .willReturn(document);
        } else {
            given(documentRepository.read(file))
                .willReturn(document);
        }
        given(citationService.getCitation(document))
            .willReturn(Optional.ofNullable(Citation.builder()
                .authors(Arrays.asList("Foo", "Bar"))
                .doi("1234:45553")
                .publisher("BOB")
                .title("Land and Water")
                .year(2020)
                .build()));
    }

    @SneakyThrows
    private String expectedResponse(String filename) {
        return StreamUtils.copyToString(
            getClass().getResourceAsStream(filename),
            StandardCharsets.UTF_8
        );
    }

    private String expectedBibtex() {
        return expectedResponse("bibtex.txt");
    }

    private String expectedRis() {
        return expectedResponse("ris.txt");
    }

    private String expectedJson() {
        return expectedResponse("citation.json");
    }

    @Test
    @DisplayName("get citation in Bibtex format from Accept header")
    void getBibtexAccept() throws Exception {
        //given
        givenUserIsPermittedToViewCurrent();
        givenCitation(false);

        //when
        mvc.perform(
            get("/documents/{file}/citation", file)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
                .accept(BIBTEX_VALUE)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(BIBTEX_VALUE))
            .andExpect(content().string(expectedBibtex()));
    }

    @Test
    @DisplayName("get citation in Bibtex format from Format query parameter")
    void getBibtexFormat() throws Exception {
        //given
        givenUserIsPermittedToViewCurrent();
        givenCitation(false);

        //when
        mvc.perform(
            get("/documents/{file}/citation", file)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
                .param("format", BIBTEX_SHORT)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(BIBTEX_VALUE))
            .andExpect(content().string(expectedBibtex()));
    }

    @Test
    @DisplayName("get citation in RIS format from Accept header")
    void getRisAccept() throws Exception {
        //given
        givenUserIsPermittedToViewCurrent();
        givenCitation(false);

        //when
        mvc.perform(
            get("/documents/{file}/citation", file)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
                .accept(RESEARCH_INFO_SYSTEMS_VALUE)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(RESEARCH_INFO_SYSTEMS_VALUE))
            .andExpect(content().string(expectedRis()));
    }

    @Test
    @DisplayName("get citation in RIS format from Format query parameter")
    void getRisFormat() throws Exception {
        //given
        givenUserIsPermittedToViewCurrent();
        givenCitation(false);

        //when
        mvc.perform(
            get("/documents/{file}/citation", file)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
                .param("format", RESEARCH_INFO_SYSTEMS_SHORT)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(RESEARCH_INFO_SYSTEMS_VALUE))
            .andExpect(content().string(expectedRis()));
    }

    @Test
    @DisplayName("get citation in JSON format from Accept header")
    void getJsonAccept() throws Exception {
        //given
        givenUserIsPermittedToViewCurrent();
        givenCitation(false);

        //when
        mvc.perform(
            get("/documents/{file}/citation", file)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson()));
    }

    @Test
    @DisplayName("get citation in JSON format from Format query parameter")
    void getJsonFormat() throws Exception {
        //given
        givenUserIsPermittedToViewCurrent();
        givenCitation(false);

        //when
        mvc.perform(
            get("/documents/{file}/citation", file)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
                .param("format", "json")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson()));
    }

    @Test
    @DisplayName("get citation in JSON format as default, no headers or parameters set")
    void getJsonDefault() throws Exception {
        //given
        givenUserIsPermittedToViewCurrent();
        givenCitation(false);

        //when
        mvc.perform(
            get("/documents/{file}/citation", file)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson()));
    }

    @Test
    @DisplayName("get historic revision of citation as JSON")
    void getHistoricCitationJson() throws Exception {
        //Given
        givenUserIsPermittedToViewHistoric();
        givenCitation(true);

        //When
        mvc.perform(
            get("/history/{revision}/{file}/citation", revision, file)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson()));
    }

    @Test
    @DisplayName("get historic revision of citation as RIS")
    void getHistoricCitationRis() throws Exception {
        //Given
        givenUserIsPermittedToViewHistoric();
        givenCitation(true);

        //When
        mvc.perform(
            get("/history/{revision}/{file}/citation", revision, file)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
                .accept(RESEARCH_INFO_SYSTEMS_VALUE)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(RESEARCH_INFO_SYSTEMS_VALUE))
            .andExpect(content().string(expectedRis()));
    }

    @Test
    @DisplayName("Non-Gemini document cannot have citation")
    void checkThatNonGeminiDocumentsFailsToGetCitation() throws Exception {
        //given
        givenUserIsPermittedToViewCurrent();
        val document = new CehModel(); // N.B. not a GeminiDocument
        given(documentRepository.read(file))
            .willReturn(document);

        //when
        mvc.perform(
            get("/documents/{file}/citation", file)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"message\":\"Only Gemini Documents can have citations\"}"));

        //then
        verifyNoInteractions(citationService);
    }

    @Test
    @DisplayName("Gemini document without a citation throws an error")
    public void checkThatResourceNotFoundIfGeminiDocumentDoesntHaveCitation() throws Exception {
        //given
        givenUserIsPermittedToViewCurrent();
        val document = new GeminiDocument();
        given(documentRepository.read(file))
            .willReturn(document);
        given(citationService.getCitation(document)).willReturn(Optional.empty());

        //when
        mvc.perform(
            get("/documents/{file}/citation", file)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"message\":\"The Document is not citable\"}"));
    }
}
