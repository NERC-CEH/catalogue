package uk.ac.ceh.gateway.catalogue.maintenance;

import com.google.common.collect.HashMultimap;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.jena.JenaIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.mapserver.MapServerIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.ADMIN;
import static uk.ac.ceh.gateway.catalogue.controllers.DocumentController.ADMIN_DELETE_ROLE;
import static uk.ac.ceh.gateway.catalogue.controllers.DocumentController.MAINTENANCE_ROLE;

/**
 * The admin delete route bypasses a record's own permissions, so the assertions that matter most here are
 * that it is unreachable without {@code ROLE_CIG_ADMIN_DELETE}, that a preview never deletes, and that
 * every confirmation guard actually refuses.
 */
@WithMockCatalogueUser(username = ADMIN, grantedAuthorities = {MAINTENANCE_ROLE, ADMIN_DELETE_ROLE})
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("AdminDeleteController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AdminDeleteControllerTest extends AbstractMvcTest {

    private static final String URL = "/maintenance/documents/delete";
    private static final String ID = "35fca77f-89ce-4c40-b581-45ed039936a4";

    @MockitoBean CachedDataRepository cachedDataRepository;
    @MockitoBean DocumentInfoMapper<MetadataInfo> metadataInfoMapper;
    @MockitoBean DocumentTypeLookupService documentTypeLookupService;
    @MockitoBean DocumentRepository documentRepository;

    // Needed by the wider context and by the maintenance page templates
    @MockitoBean DataRepositoryOptimizingService repoService;
    @MockitoBean @Qualifier("solr-index") SolrIndexingService indexService;
    @MockitoBean @Qualifier("jena-index") JenaIndexingService linkingService;
    @MockitoBean @Qualifier("mapserver-index") MapServerIndexingService mapserverService;
    @MockitoBean CatalogueService catalogueService;
    @MockitoBean ProfileService profileService;

    @Autowired private Configuration configuration;

    @SneakyThrows
    @BeforeEach
    void givenFreemarkerAndCatalogue() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("profile", profileService);
        given(catalogueService.defaultCatalogue()).willReturn(
            Catalogue.builder().id("eidc").title("Env Data Centre").url("https://example.com")
                .contactUrl("").logo("eidc.png").build());
    }

    /** Stubs the datastore so {@code id} exists with the given .meta facts. */
    @SneakyThrows
    private void givenRecord(String path, String documentType, String state, String catalogue, boolean typeRegistered) {
        var permissions = HashMultimap.<Permission, String>create();
        permissions.put(Permission.VIEW, "phtr");
        permissions.put(Permission.DELETE, "phtr");
        var info = MetadataInfo.builder()
            .rawType("application/json").state(state).documentType(documentType)
            .catalogue(catalogue).permissions(permissions).build();

        given(cachedDataRepository.getLatestRevisionId()).willReturn("rev1");
        given(cachedDataRepository.readLatest("rev1", path + ".meta")).willReturn("{}".getBytes());
        given(metadataInfoMapper.readInfo(any())).willReturn(info);
        if (typeRegistered) {
            // willAnswer rather than willReturn: getType's return is Class<? extends MetadataDocument>,
            // whose captured wildcard will not accept a concrete Class literal directly.
            given(documentTypeLookupService.getType(documentType)).willAnswer(invocation -> GeminiDocument.class);
        } else {
            given(documentTypeLookupService.getType(documentType))
                .willThrow(new IllegalArgumentException(documentType + ": does not have a corresponding class"));
        }
    }

    @SneakyThrows
    private void givenRawPresent(String path, int size) {
        given(cachedDataRepository.readLatest("rev1", path + ".raw")).willReturn(new byte[size]);
    }

    @SneakyThrows
    private void givenRawMissing(String path) {
        given(cachedDataRepository.readLatest("rev1", path + ".raw"))
            .willThrow(new GitFileNotFoundException("no such file"));
    }

    // ---------------------------------------------------------------- access

    @Test
    @SneakyThrows
    @DisplayName("the form is reachable with the role")
    void formIsReachableWithTheRole() {
        mvc.perform(get(URL)).andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser(username = ADMIN, grantedAuthorities = MAINTENANCE_ROLE)
    @DisplayName("every endpoint is forbidden without the role, even for a maintenance admin")
    void forbiddenWithoutTheRole() {
        mvc.perform(get(URL)).andExpect(status().isForbidden());
        mvc.perform(post(URL + "/preview").with(csrf())
            .param("location", "METADATA_RECORD").param("id", ID)).andExpect(status().isForbidden());
        mvc.perform(post(URL + "/confirm").with(csrf())
            .param("location", "METADATA_RECORD").param("id", ID)).andExpect(status().isForbidden());
        verify(documentRepository, never()).delete(any(), any(), any());
    }

    // ---------------------------------------------------------------- preview

    @Test
    @SneakyThrows
    @DisplayName("preview reports the record and deletes nothing")
    void previewReportsAndDeletesNothing() {
        givenRecord(ID, "GEMINI_DOCUMENT", "published", "eidc", true);
        givenRawPresent(ID, 801);

        mvc.perform(post(URL + "/preview").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.found").value(true))
            .andExpect(jsonPath("$.documentType").value("GEMINI_DOCUMENT"))
            .andExpect(jsonPath("$.state").value("published"))
            .andExpect(jsonPath("$.catalogue").value("eidc"))
            .andExpect(jsonPath("$.rawPresent").value(true))
            .andExpect(jsonPath("$.rawSize").value(801));

        verify(documentRepository, never()).delete(any(), any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("preview flags a document type that is no longer registered")
    void previewFlagsAnUnregisteredType() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);

        mvc.perform(post(URL + "/preview").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.found").value(true))
            .andExpect(jsonPath("$.documentTypeRegistered").value(false));
    }

    @Test
    @SneakyThrows
    @DisplayName("preview reports a missing body rather than failing")
    void previewReportsMissingBody() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);

        mvc.perform(post(URL + "/preview").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.found").value(true))
            .andExpect(jsonPath("$.rawPresent").value(false));
    }

    /**
     * The same endpoints serve the form and a JSON API by content negotiation; the tests above assert the
     * model, so this one proves the template itself renders and surfaces the warnings an operator relies
     * on before confirming.
     */
    @Test
    @SneakyThrows
    @DisplayName("the preview page renders the orphan and published warnings")
    void previewPageRendersTheWarnings() {
        givenRecord(ID, "methodrecord", "published", "ukceh", false);
        givenRawMissing(ID);

        var content = mvc.perform(post(URL + "/preview").with(csrf())
                .accept(MediaType.TEXT_HTML)
                .param("location", "METADATA_RECORD").param("id", ID))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(content, containsString("not registered"));
        assertThat(content, containsString("This record is published"));
        assertThat(content, containsString("Retype the record id"));
        // the published record's catalogue must be typed, so the field has to be on the page
        assertThat(content, containsString("confirmCatalogue"));
    }

    @Test
    @SneakyThrows
    @DisplayName("a non-UUID id is rejected before the datastore is touched")
    void rejectsANonUuidId() {
        var content = mvc.perform(post(URL + "/preview").with(csrf())
                .accept(MediaType.TEXT_HTML)
                .param("location", "METADATA_RECORD").param("id", "../../etc/passwd"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(content, containsString("not a valid record id"));
        verify(cachedDataRepository, never()).readLatest(any(), any());
    }

    // ---------------------------------------------------------------- confirm guards

    @Test
    @SneakyThrows
    @DisplayName("confirm refuses when the retyped id does not match")
    void refusesMismatchedRetypedId() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);

        var content = mvc.perform(post(URL + "/confirm").with(csrf())
                .accept(MediaType.TEXT_HTML)
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", "35fca77f-0000-0000-0000-000000000000")
                .param("reason", "orphaned"))
            .andReturn().getResponse().getContentAsString();

        assertThat(content, containsString("retyped id does not match"));
        verify(documentRepository, never()).delete(any(), any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("confirm refuses without a reason")
    void refusesBlankReason() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);

        var content = mvc.perform(post(URL + "/confirm").with(csrf())
                .accept(MediaType.TEXT_HTML)
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "   "))
            .andReturn().getResponse().getContentAsString();

        assertThat(content, containsString("reason is required"));
        verify(documentRepository, never()).delete(any(), any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("a published record needs its catalogue id typed as well")
    void publishedNeedsCatalogueConfirmation() {
        givenRecord(ID, "GEMINI_DOCUMENT", "published", "eidc", true);
        givenRawPresent(ID, 10);

        var content = mvc.perform(post(URL + "/confirm").with(csrf())
                .accept(MediaType.TEXT_HTML)
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "superseded")
                .param("confirmCatalogue", "wrong"))
            .andReturn().getResponse().getContentAsString();

        assertThat(content, containsString("catalogue id must be typed"));
        verify(documentRepository, never()).delete(any(), any(), any());
    }

    // ---------------------------------------------------------------- confirm success

    @Test
    @SneakyThrows
    @DisplayName("confirm deletes once, recording the path and reason in the commit message")
    void deletesRecordingPathAndReason() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);

        mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "orphaned by removal of the type"))
            .andExpect(status().isOk());

        var messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(documentRepository).delete(any(), eq(ID), messageCaptor.capture());
        assertThat(messageCaptor.getValue(), containsString(ID));
        assertThat(messageCaptor.getValue(), containsString("orphaned by removal of the type"));
        assertThat(messageCaptor.getValue(), not(containsString("PUBLISHED")));
    }

    @Test
    @SneakyThrows
    @DisplayName("deleting a published record marks the commit message")
    void marksPublishedInTheCommitMessage() {
        givenRecord(ID, "GEMINI_DOCUMENT", "published", "eidc", true);
        givenRawPresent(ID, 10);

        mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "superseded")
                .param("confirmCatalogue", "eidc"))
            .andExpect(status().isOk());

        var messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(documentRepository).delete(any(), eq(ID), messageCaptor.capture());
        assertThat(messageCaptor.getValue(), containsString("PUBLISHED"));
    }

    @Test
    @SneakyThrows
    @DisplayName("a service agreement is deleted at its prefixed path")
    void deletesAServiceAgreementAtItsPrefixedPath() {
        var path = "service-agreement/" + ID;
        givenRecord(path, "service-agreement", "draft", "eidc", true);
        givenRawMissing(path);

        mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "SERVICE_AGREEMENT").param("id", ID)
                .param("confirmId", ID).param("reason", "stranded"))
            .andExpect(status().isOk());

        verify(documentRepository).delete(any(), eq(path), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("the legacy service agreement directory is reachable")
    void deletesFromTheLegacyServiceAgreementDirectory() {
        var path = "service-agreements/" + ID;
        givenRecord(path, "service-agreement", "draft", "eidc", true);
        givenRawMissing(path);

        mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "SERVICE_AGREEMENT_LEGACY").param("id", ID)
                .param("confirmId", ID).param("reason", "stranded by the path change"))
            .andExpect(status().isOk());

        verify(documentRepository).delete(any(), eq(path), any());
    }

    // ---------------------------------------------------------------- CSRF

    @Test
    @SneakyThrows
    @DisplayName("a POST without a CSRF token is refused")
    void refusesAPostWithoutACsrfToken() {
        mvc.perform(post(URL + "/confirm")
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "orphaned"))
            .andExpect(status().isForbidden());
        verify(documentRepository, never()).delete(any(), any(), any());
    }

    /**
     * The regression guard for scoping CSRF: enforcement is narrowed to this route, so every other POST
     * in the application must keep working without a token.
     */
    @Test
    @SneakyThrows
    @DisplayName("other maintenance POSTs still work without a CSRF token")
    void otherPostsAreUnaffectedByCsrf() {
        mvc.perform(post("/maintenance/documents/reindex"))
            .andExpect(status().isOk());
    }
}
