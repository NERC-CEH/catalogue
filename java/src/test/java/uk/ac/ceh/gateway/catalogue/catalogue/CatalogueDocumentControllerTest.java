package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataConflictException;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.MetadataPreconditionRequiredException;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.BeforeEach;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("CatalogueDocumentController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})

public @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CatalogueDocumentControllerTest extends AbstractMvcTest {
    private @MockitoBean DocumentRepository documentRepository;
    private @MockitoBean(name="permission") PermissionService permissionService;
    private @MockitoBean CatalogueService catalogueService;

    @MockitoBean private CachedDataRepository cachedDataRepository;
    private CatalogueDocumentController controller;

    private final String file = "955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052";

    @BeforeEach
    void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue()).willReturn(
            Catalogue.builder().id("eidc").title("EIDC").url("https://eidc.ceh.ac.uk").contactUrl("").logo("").build()
        );
    }

    @BeforeEach
    void setup() {
        controller = new CatalogueDocumentController(documentRepository, catalogueService, cachedDataRepository);
    }

    @SneakyThrows
    private void givenMetadataDocument() {
        val document = new GeminiDocument();
        document.setId(file);
        document.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file))
            .willReturn(document);
    }

    @SneakyThrows
    private void givenMetadataDocumentWithCatalogueView() {
        val document = new GeminiDocument();
        document.setId(file);
        document.setMetadata(
            MetadataInfo.builder()
                .catalogue("eidc")
                .catalogueView(List.of("ukceh", "assist"))
                .build()
        );
        given(documentRepository.read(file)).willReturn(document);
    }

    private void givenKnownCatalogues() {
        given(catalogueService.retrieveAll()).willReturn(List.of(
            Catalogue.builder().id("eidc").title("EIDC").url("").contactUrl("").logo("").build(),
            Catalogue.builder().id("ukceh").title("UKCEH").url("").contactUrl("").logo("").build(),
            Catalogue.builder().id("assist").title("ASSIST").url("").contactUrl("").logo("").build()
        ));
    }

    private void givenUserCanView() {
        given(permissionService.userCanView(file))
            .willReturn(true);
    }

    private void givenUserCanNotView() {
        given(permissionService.userCanView(file))
            .willReturn(false);
    }

    private void givenUserCanEdit() {
        given(permissionService.userCanEdit(file))
            .willReturn(true);
    }

    @Test
    public void getCurrentCatalogue() throws Exception {
        //Given
        givenUserCanView();
        givenMetadataDocument();
        val expectedResponse = """
            {
                "id": "955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052",
                "value": "eidc"
            }
            """;

        //When
        mvc.perform(
                get("/documents/{file}/catalogue", file)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));

        //Then
    }

    @SneakyThrows
    @Test
    public void getUnknownFile() {
        //Given
        givenUserCanNotView();

        //When
        mvc.perform(
                get("/documents/{file}/catalogue", file)
            )
            .andExpect(status().isForbidden());

        //Then
    }

    @Test
    public void getCatalogueEmitsETagOfCurrentRevision() throws Exception {
        //Given a readable document and a known per-document revision
        givenMetadataDocument();
        given(cachedDataRepository.getDocumentRevisionToken(file)).willReturn("rev1");

        //When reading the current catalogue
        ResponseEntity<CatalogueResource> actual = controller.currentCatalogue(file);

        //Then the ETag carries the current revision (quoted per HTTP)
        assertThat(actual.getHeaders().getETag(), is("\"rev1\""));
    }

    @Test
    public void getCatalogueOmitsETagWhenNoRevisionIsKnown() throws Exception {
        //Given a readable document with no known revision (e.g. a brand-new document)
        givenMetadataDocument();
        given(cachedDataRepository.getDocumentRevisionToken(file)).willReturn(null);

        //When reading the current catalogue
        ResponseEntity<CatalogueResource> actual = controller.currentCatalogue(file);

        //Then no ETag header is set
        assertThat(actual.getHeaders().getETag(), equalTo(null));
    }

    @Test
    public void updateCatalogue() throws Exception {
        //Given
        givenUserCanEdit();
        val user = new CatalogueUser("test","test@eample.com");

        val document = new GeminiDocument()
            .setId(file)
            .setMetadata(
                MetadataInfo.builder()
                .catalogue("eidc")
                .build()
        );
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(
            user,
            document,
            file,
            "Catalogues of 955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052 changed.",
            "rev1"
        )).willReturn(document);

        //When
        mvc.perform(
                put("/documents/{file}/catalogue", file)
                    .contentType(APPLICATION_JSON)
                    .header(HttpHeaders.IF_MATCH, "\"rev1\"")
                    .content("""
                        {"id": "1", "value": "eidc"}
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON));

        //Then
    }

    @Test
    public void updateCatalogueWithoutIfMatchIsRejectedAsPreconditionRequired() {
        //Given an update with no If-Match header
        val user = new CatalogueUser("test", "test@example.com");
        val resource = new CatalogueResource(file, "eidc");

        //When/Then updating without the precondition header is refused
        assertThrows(MetadataPreconditionRequiredException.class, () ->
            controller.updateCatalogue(user, file, resource, null));
    }

    @Test
    public void updateCatalogueWithIfMatchSavesWithThatRevision() throws Exception {
        //Given a matching read and a stubbed save
        val user = new CatalogueUser("test", "test@example.com");
        val document = new GeminiDocument()
            .setId(file)
            .setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(eq(user), eq(document), eq(file), any(), eq("rev1"))).willReturn(document);

        //When updating with an If-Match
        controller.updateCatalogue(user, file, new CatalogueResource(file, "eidc"), "\"rev1\"");

        //Then the (unquoted) revision is passed to the repository
        verify(documentRepository).save(eq(user), eq(document), eq(file), any(), eq("rev1"));
    }

    @Test
    public void updateCatalogueEmitsETagOfNewRevisionAfterSave() throws Exception {
        //Given a matching read, a stubbed save, and the post-save revision
        val user = new CatalogueUser("test", "test@example.com");
        val document = new GeminiDocument()
            .setId(file)
            .setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(eq(user), eq(document), eq(file), any(), eq("rev1"))).willReturn(document);
        given(cachedDataRepository.getDocumentRevisionToken(file)).willReturn("rev2");

        //When updating with an If-Match
        ResponseEntity<CatalogueResource> actual =
            controller.updateCatalogue(user, file, new CatalogueResource(file, "eidc"), "\"rev1\"");

        //Then the response carries the NEW per-document revision as its ETag (quoted per HTTP)
        assertThat(actual.getHeaders().getETag(), is("\"rev2\""));
    }

    @SneakyThrows
    @Test
    public void updateCataloguePropagatesConflictExceptionFromRepository() {
        //Given a stale revision that the repository rejects as a conflict
        val user = new CatalogueUser("test", "test@example.com");
        val document = new GeminiDocument()
            .setId(file)
            .setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(eq(user), eq(document), eq(file), any(), eq("stale-rev")))
            .willThrow(new MetadataConflictException("stale", document));

        //When/Then the conflict propagates to the caller (central handler maps it to 409)
        assertThrows(MetadataConflictException.class, () ->
            controller.updateCatalogue(user, file, new CatalogueResource(file, "eidc"), "\"stale-rev\""));
    }

    @Test
    public void getCurrentCatalogueView() throws Exception {
        //Given
        givenUserCanView();
        givenMetadataDocumentWithCatalogueView();
        val expectedResponse = """
            {
                "id": "955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052",
                "value": ["ukceh", "assist"]
            }
            """;

        //When
        mvc.perform(get("/documents/{file}/catalogue-view", file))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));
    }

    @Test
    public void getCatalogueViewForbiddenWhenNoViewPermission() throws Exception {
        //Given
        givenUserCanNotView();

        //When
        mvc.perform(get("/documents/{file}/catalogue-view", file))
            .andExpect(status().isForbidden());
    }

    @Test
    public void getCatalogueViewEmitsETagOfCurrentRevision() throws Exception {
        //Given a readable document and a known per-document revision
        givenMetadataDocumentWithCatalogueView();
        given(cachedDataRepository.getDocumentRevisionToken(file)).willReturn("rev1");

        //When reading the current catalogue view
        ResponseEntity<CatalogueViewResource> actual = controller.currentCatalogueView(file);

        //Then the ETag carries the current revision (quoted per HTTP)
        assertThat(actual.getHeaders().getETag(), is("\"rev1\""));
    }

    @Test
    public void getCatalogueViewOmitsETagWhenNoRevisionIsKnown() throws Exception {
        //Given a readable document with no known revision (e.g. a brand-new document)
        givenMetadataDocumentWithCatalogueView();
        given(cachedDataRepository.getDocumentRevisionToken(file)).willReturn(null);

        //When reading the current catalogue view
        ResponseEntity<CatalogueViewResource> actual = controller.currentCatalogueView(file);

        //Then no ETag header is set
        assertThat(actual.getHeaders().getETag(), equalTo(null));
    }

    @SneakyThrows
    @Test
    public void updateCatalogueViewFiltersOutPrimaryAndUnknownCatalogues() throws Exception {
        //Given
        givenUserCanEdit();
        givenKnownCatalogues();
        val user = new CatalogueUser("test", "test@example.com");
        val document = new GeminiDocument()
            .setId(file)
            .setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(
            user,
            document,
            file,
            "Secondary catalogues of 955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052 changed.",
            "rev1"
        )).willReturn(document);

        //When — sending "eidc" (primary, filtered out) and "unknown" (not in catalogue service)
        mvc.perform(
            put("/documents/{file}/catalogue-view", file)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.IF_MATCH, "\"rev1\"")
                .content("""
                    {"id": "955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052", "value": ["ukceh", "eidc", "unknown"]}
                    """)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void updateCatalogueViewWithoutIfMatchIsRejectedAsPreconditionRequired() {
        //Given an update with no If-Match header
        val user = new CatalogueUser("test", "test@example.com");
        val resource = new CatalogueViewResource(file, List.of("ukceh"));

        //When/Then updating without the precondition header is refused
        assertThrows(MetadataPreconditionRequiredException.class, () ->
            controller.updateCatalogueView(user, file, resource, null));
    }

    @Test
    public void updateCatalogueViewWithIfMatchSavesWithThatRevision() throws Exception {
        //Given a matching read and a stubbed save
        givenKnownCatalogues();
        val user = new CatalogueUser("test", "test@example.com");
        val document = new GeminiDocument()
            .setId(file)
            .setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(eq(user), eq(document), eq(file), any(), eq("rev1"))).willReturn(document);

        //When updating with an If-Match
        controller.updateCatalogueView(user, file, new CatalogueViewResource(file, List.of("ukceh")), "\"rev1\"");

        //Then the (unquoted) revision is passed to the repository
        verify(documentRepository).save(eq(user), eq(document), eq(file), any(), eq("rev1"));
    }

    @Test
    public void updateCatalogueViewEmitsETagOfNewRevisionAfterSave() throws Exception {
        //Given a matching read, a stubbed save, and the post-save revision
        givenKnownCatalogues();
        val user = new CatalogueUser("test", "test@example.com");
        val document = new GeminiDocument()
            .setId(file)
            .setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(eq(user), eq(document), eq(file), any(), eq("rev1"))).willReturn(document);
        given(cachedDataRepository.getDocumentRevisionToken(file)).willReturn("rev2");

        //When updating with an If-Match
        ResponseEntity<CatalogueViewResource> actual =
            controller.updateCatalogueView(user, file, new CatalogueViewResource(file, List.of("ukceh")), "\"rev1\"");

        //Then the response carries the NEW per-document revision as its ETag (quoted per HTTP)
        assertThat(actual.getHeaders().getETag(), is("\"rev2\""));
    }

    @SneakyThrows
    @Test
    public void updateCatalogueViewPropagatesConflictExceptionFromRepository() {
        //Given a stale revision that the repository rejects as a conflict
        givenKnownCatalogues();
        val user = new CatalogueUser("test", "test@example.com");
        val document = new GeminiDocument()
            .setId(file)
            .setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(eq(user), eq(document), eq(file), any(), eq("stale-rev")))
            .willThrow(new MetadataConflictException("stale", document));

        //When/Then the conflict propagates to the caller (central handler maps it to 409)
        assertThrows(MetadataConflictException.class, () ->
            controller.updateCatalogueView(user, file, new CatalogueViewResource(file, List.of("ukceh")), "\"stale-rev\""));
    }

}
