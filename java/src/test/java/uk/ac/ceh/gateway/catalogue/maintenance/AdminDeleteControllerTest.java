package uk.ac.ceh.gateway.catalogue.maintenance;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.HashMultimap;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
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
import uk.ac.ceh.gateway.catalogue.metrics.MetricsService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @MockitoBean MetricsService metricsService;

    // Needed by the wider context and by the maintenance page templates
    @MockitoBean DataRepositoryOptimizingService repoService;
    @MockitoBean @Qualifier("solr-index") SolrIndexingService indexService;
    @MockitoBean @Qualifier("jena-index") JenaIndexingService linkingService;
    @MockitoBean @Qualifier("mapserver-index") MapServerIndexingService mapserverService;
    @MockitoBean CatalogueService catalogueService;
    @MockitoBean ProfileService profileService;

    @Autowired private Configuration configuration;

    private ListAppender<ILoggingEvent> logAppender;

    @SneakyThrows
    @BeforeEach
    void givenFreemarkerAndCatalogue() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("profile", profileService);
        given(catalogueService.defaultCatalogue()).willReturn(
            Catalogue.builder().id("eidc").title("Env Data Centre").url("https://example.com")
                .contactUrl("").logo("eidc.png").build());
    }

    @BeforeEach
    void captureLogs() {
        logAppender = new ListAppender<>();
        logAppender.start();
        ((Logger) LoggerFactory.getLogger(AdminDeleteController.class)).addAppender(logAppender);
    }

    @AfterEach
    void stopCapturingLogs() {
        ((Logger) LoggerFactory.getLogger(AdminDeleteController.class)).detachAppender(logAppender);
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

    /** Looks a record up as the form does, returning the rendered page. */
    @SneakyThrows
    private String preview(String id) {
        return mvc.perform(post(URL + "/preview").with(csrf())
                .param("location", "METADATA_RECORD").param("id", id))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }

    /** Matches the {@code CatalogueUser} passed to a mock by username, for asserting who performed a delete. */
    private static org.mockito.ArgumentMatcher<CatalogueUser> byUsername(String username) {
        return user -> user != null && username.equals(user.getUsername());
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

        var content = preview(ID);

        assertThat(content, containsString("Review before deleting"));
        assertThat(content, containsString("GEMINI_DOCUMENT"));
        assertThat(content, containsString("present (801 bytes)"));
        assertThat(content, not(containsString("not registered")));
        // matched inside the fact list, since the page mentions the catalogue and the state elsewhere too
        assertThat(content, containsString(">published</dd>"));
        assertThat(content, containsString(">eidc</dd>"));
        verify(documentRepository, never()).delete(any(), any(), any());
    }

    /**
     * The orphan case, and the one the feature exists for: the type no longer resolves and — as with 28 of
     * the 29 records found on staging — there is no body either. Both must be reported, not thrown.
     */
    @Test
    @SneakyThrows
    @DisplayName("preview flags an unregistered type and a missing body")
    void previewFlagsAnOrphan() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);

        var content = preview(ID);

        assertThat(content, containsString("Review before deleting"));
        assertThat(content, containsString("not registered"));
        assertThat(content, containsString("<em>missing</em>"));
    }

    /**
     * The other half of the orphan picture #239/#252 describe: a document already deleted from git, but
     * still referenced by the metrics tables. Before this fix, {@code describe()} stopped at "No record
     * found" here, and there was no way to reach {@code performDelete} to clean the metrics row up.
     */
    @Test
    @SneakyThrows
    @DisplayName("preview reports a metrics-only orphan when the git record is gone but metrics remain")
    void previewReportsAMetricsOnlyOrphan() {
        given(cachedDataRepository.getLatestRevisionId()).willReturn("rev1");
        given(cachedDataRepository.readLatest("rev1", ID + ".meta"))
            .willThrow(new GitFileNotFoundException("no such file"));
        given(metricsService.hasMetricsFor(ID)).willReturn(true);

        var content = preview(ID);

        assertThat(content, containsString("Review before deleting"));
        assertThat(content, containsString("No metadata record exists"));
        verify(documentRepository, never()).delete(any(), any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("the review step tells the operator both the record and its metrics will be deleted")
    void reviewStepMentionsBothRecordAndMetrics() {
        givenRecord(ID, "GEMINI_DOCUMENT", "published", "eidc", true);
        givenRawPresent(ID, 10);
        given(metricsService.hasMetricsFor(ID)).willReturn(true);

        var content = preview(ID);

        assertThat(content, containsString("metadata record and its recorded metrics"));
    }

    @Test
    @SneakyThrows
    @DisplayName("the review step says only metrics will be deleted when there is no metadata record")
    void reviewStepMentionsMetricsOnly() {
        given(cachedDataRepository.getLatestRevisionId()).willReturn("rev1");
        given(cachedDataRepository.readLatest("rev1", ID + ".meta"))
            .willThrow(new GitFileNotFoundException("no such file"));
        given(metricsService.hasMetricsFor(ID)).willReturn(true);

        var content = preview(ID);

        assertThat(content, containsString("recorded metrics"));
        assertThat(content, containsString("No metadata record exists"));
    }

    @Test
    @SneakyThrows
    @DisplayName("the preview warns before a published record can be confirmed")
    void previewWarnsAboutAPublishedRecord() {
        givenRecord(ID, "GEMINI_DOCUMENT", "published", "eidc", true);
        givenRawPresent(ID, 10);

        var content = preview(ID);

        assertThat(content, containsString("This record is published"));
        assertThat(content, containsString("Retype the record id"));
        // the published record's catalogue must be typed, so the field has to be on the page
        assertThat(content, containsString("confirmCatalogue"));
    }

    @Test
    @SneakyThrows
    @DisplayName("a non-UUID id is rejected before the datastore is touched")
    void rejectsANonUuidId() {
        assertThat(preview("../../etc/passwd"), containsString("not a valid record id"));
        verify(cachedDataRepository, never()).readLatest(any(), any());
    }

    /**
     * {@code response.setId(id)} is set from the raw request parameter before {@code isValidId} is
     * checked, and step 1 of the form always re-renders {@code value="${id!''}"} with that value — on
     * this invalid-id path as much as any other. This is the regression test for the FreeMarker
     * {@code output_format="HTML"} fix in {@code delete.ftlh}: without it, this id breaks out of the
     * attribute and injects a script tag.
     */
    @Test
    @SneakyThrows
    @DisplayName("an id containing markup is escaped in the re-rendered form, not injected")
    void escapesAnUnsafeIdInTheForm() {
        var content = preview("\"><script>alert(1)</script>");

        assertThat(content, containsString("not a valid record id"));
        assertThat(content, not(containsString("<script>alert(1)</script>")));
        assertThat(content, containsString("&lt;script&gt;alert(1)&lt;/script&gt;"));
    }

    /**
     * A well-formed but nonexistent id is arguably the most realistic operator mistake — a mistyped id
     * that still happens to look like a UUID — and is a materially different failure than a malformed one.
     */
    @Test
    @SneakyThrows
    @DisplayName("preview reports no record found for a well-formed id that doesn't exist, and logs it")
    void previewReportsNoRecordFoundForANonexistentId() {
        given(cachedDataRepository.getLatestRevisionId()).willReturn("rev1");
        given(cachedDataRepository.readLatest("rev1", ID + ".meta"))
            .willThrow(new GitFileNotFoundException("no such file"));

        var content = preview(ID);

        assertThat(content, containsString("No record found at " + ID));
        assertThat(logAppender.list, hasSize(1));
        assertThat(logAppender.list.getFirst().getLevel(), is(Level.WARN));
        assertThat(logAppender.list.getFirst().getFormattedMessage(), containsString(ID));
    }

    /**
     * This is a form, not an API — the safeguards are the whole point, and a JSON caller would be a way
     * around them. {@code produces} on the mapping is what keeps content negotiation from offering one.
     */
    @Test
    @SneakyThrows
    @DisplayName("the route does not serve JSON")
    void doesNotServeJson() {
        mvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotAcceptable());
        mvc.perform(post(URL + "/preview").with(csrf()).accept(MediaType.APPLICATION_JSON)
                .param("location", "METADATA_RECORD").param("id", ID))
            .andExpect(status().isNotAcceptable());
    }

    // ---------------------------------------------------------------- confirm guards

    @Test
    @SneakyThrows
    @DisplayName("confirm refuses when the retyped id does not match")
    void refusesMismatchedRetypedId() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);

        var content = mvc.perform(post(URL + "/confirm").with(csrf())
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
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "superseded")
                .param("confirmCatalogue", "wrong"))
            .andReturn().getResponse().getContentAsString();

        assertThat(content, containsString("catalogue id must be typed"));
        verify(documentRepository, never()).delete(any(), any(), any());
    }

    /** Pins the confirmation as case-insensitive, positively — {@code publishedNeedsCatalogueConfirmation}
     *  above only tests an outright-wrong value, and {@code marksPublishedInTheCommitMessage} below only
     *  tests an exact-case match, so neither would notice a regression to case-sensitive equality. */
    @Test
    @SneakyThrows
    @DisplayName("the catalogue id confirmation is case-insensitive")
    void acceptsADifferentlyCasedCatalogueId() {
        givenRecord(ID, "GEMINI_DOCUMENT", "published", "eidc", true);
        givenRawPresent(ID, 10);

        mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "superseded")
                .param("confirmCatalogue", "EIDC"))
            .andExpect(status().isOk());

        verify(documentRepository).delete(argThat(byUsername(ADMIN)), eq(ID), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("confirm refuses a well-formed id that has no record, without reaching the delete guards")
    void confirmRefusesANonexistentRecord() {
        given(cachedDataRepository.getLatestRevisionId()).willReturn("rev1");
        given(cachedDataRepository.readLatest("rev1", ID + ".meta"))
            .willThrow(new GitFileNotFoundException("no such file"));

        var content = mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "orphaned"))
            .andReturn().getResponse().getContentAsString();

        assertThat(content, containsString("No record found at " + ID));
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
        // The user matters as much as the path and reason: the controller's own Javadoc calls the git
        // log "the audit trail for deletions", so this is who performed it, not just what happened.
        verify(documentRepository).delete(argThat(byUsername(ADMIN)), eq(ID), messageCaptor.capture());
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
        verify(documentRepository).delete(argThat(byUsername(ADMIN)), eq(ID), messageCaptor.capture());
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

        verify(documentRepository).delete(argThat(byUsername(ADMIN)), eq(path), any());
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

        verify(documentRepository).delete(argThat(byUsername(ADMIN)), eq(path), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("confirm deletes both the document and its metrics when both exist")
    void confirmDeletesBothDocumentAndMetrics() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);
        given(metricsService.hasMetricsFor(ID)).willReturn(true);
        given(metricsService.deleteMetricsFor(ID)).willReturn(true);

        var content = mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "orphaned"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        verify(documentRepository).delete(argThat(byUsername(ADMIN)), eq(ID), any());
        verify(metricsService).deleteMetricsFor(ID);
        assertThat(content, containsString("Deleted " + ID));
        assertThat(content, containsString("Deleted metrics for " + ID));
    }

    /** The metrics-only orphan case #252 exists for: no git document, but a dangling metrics row. */
    @Test
    @SneakyThrows
    @DisplayName("confirm deletes the metrics even though there is no git document")
    void confirmDeletesMetricsOnlyWhenNoDocumentExists() {
        given(cachedDataRepository.getLatestRevisionId()).willReturn("rev1");
        given(cachedDataRepository.readLatest("rev1", ID + ".meta"))
            .willThrow(new GitFileNotFoundException("no such file"));
        given(metricsService.hasMetricsFor(ID)).willReturn(true);
        given(metricsService.deleteMetricsFor(ID)).willReturn(true);

        var content = mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "dangling metrics"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(content, containsString("Deleted metrics for " + ID));
        verify(documentRepository, never()).delete(any(), any(), any());
        verify(metricsService).deleteMetricsFor(ID);
    }

    /**
     * A metrics-only delete has no git document, so there is no commit for it either — log aggregation
     * is the *only* place this action can ever be recorded. Before this, the metrics branch had no log
     * line at all (unlike the document branch, which already warn-logs for exactly this audit reason),
     * so it left no trace anywhere.
     */
    @Test
    @SneakyThrows
    @DisplayName("a metrics-only delete is still logged, since there is no git commit to record it")
    void metricsOnlyDeleteIsLogged() {
        given(cachedDataRepository.getLatestRevisionId()).willReturn("rev1");
        given(cachedDataRepository.readLatest("rev1", ID + ".meta"))
            .willThrow(new GitFileNotFoundException("no such file"));
        given(metricsService.hasMetricsFor(ID)).willReturn(true);
        given(metricsService.deleteMetricsFor(ID)).willReturn(true);

        mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "dangling metrics"))
            .andExpect(status().isOk());

        assertThat(logAppender.list, hasSize(1));
        assertThat(logAppender.list.getFirst().getLevel(), is(Level.WARN));
        assertThat(logAppender.list.getFirst().getFormattedMessage(), containsString(ID));
        assertThat(logAppender.list.getFirst().getFormattedMessage(), containsString("dangling metrics"));
    }

    /** Neither deletion should be able to block the other — this is the independence #252 asked for. */
    @Test
    @SneakyThrows
    @DisplayName("confirm still deletes the document even when the metrics deletion fails")
    void confirmDeletesDocumentEvenWhenMetricsDeletionFails() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);
        given(metricsService.hasMetricsFor(ID)).willReturn(true);
        given(metricsService.deleteMetricsFor(ID)).willThrow(new RuntimeException("db unavailable"));

        var content = mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "orphaned"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        verify(documentRepository).delete(argThat(byUsername(ADMIN)), eq(ID), any());
        assertThat(content, containsString("Deleted " + ID));
        assertThat(content, containsString("Could not delete metrics"));
    }

    @Test
    @SneakyThrows
    @DisplayName("confirm still deletes the metrics even when the document delete fails")
    void confirmDeletesMetricsEvenWhenDocumentDeletionFails() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);
        given(metricsService.hasMetricsFor(ID)).willReturn(true);
        given(metricsService.deleteMetricsFor(ID)).willReturn(true);
        given(documentRepository.delete(any(), eq(ID), any()))
            .willThrow(new DocumentRepositoryException("git write failed", null));

        var content = mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "orphaned"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        verify(metricsService).deleteMetricsFor(ID);
        assertThat(content, containsString("Deleted metrics for " + ID));
        assertThat(content, containsString("Could not delete " + ID));
    }

    /**
     * Regression guard for the id staying in the lookup form after a successful delete: it used to be
     * left in place, so the next operator either had to clear it by hand or risked re-submitting it.
     */
    @Test
    @SneakyThrows
    @DisplayName("a successful delete clears the id from the re-rendered lookup form")
    void successfulDeleteClearsTheIdField() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);

        var content = mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "orphaned"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(content, not(containsString("value=\"" + ID + "\"")));
    }

    // ---------------------------------------------------------------- confirm failure

    /** performDelete's own catch block: a repository failure must be reported, not crash the request. */
    @Test
    @SneakyThrows
    @DisplayName("confirm reports an error, not a crash, when the delete itself fails")
    void confirmReportsAnErrorWhenTheDeleteFails() {
        givenRecord(ID, "methodrecord", "pending", "ukceh", false);
        givenRawMissing(ID);
        given(documentRepository.delete(any(), eq(ID), any()))
            .willThrow(new DocumentRepositoryException("git write failed", null));

        var content = mvc.perform(post(URL + "/confirm").with(csrf())
                .param("location", "METADATA_RECORD").param("id", ID)
                .param("confirmId", ID).param("reason", "orphaned"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(content, containsString("Could not delete"));
        assertThat(content, not(containsString("Deleted " + ID)));
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

    /*
     * NOT COVERED HERE: that the token cookie survives from the form to the POST.
     *
     * This is worth knowing about, because it is where the feature first broke. Authentication is
     * per-request in this application, so Spring's default CsrfAuthenticationStrategy deleted the stored
     * token on every authenticated request while only *deferring* its replacement — loading the form
     * destroyed its own token as soon as the page's stylesheet and logo were fetched, and the POST was
     * then rejected with a 403. SecurityConfig replaces that strategy with a no-op.
     *
     * MockMvc cannot assert it: SecurityMockMvcRequestPostProcessors.csrf() calls
     * WebTestUtils.setCsrfTokenRepository, which swaps the live CsrfFilter's repository for a
     * session-backed test one, and the context is shared across this class. Any assertion about the real
     * cookie here would be about the test repository instead. It needs a browser, or an end-to-end test
     * over real HTTP.
     */
}
