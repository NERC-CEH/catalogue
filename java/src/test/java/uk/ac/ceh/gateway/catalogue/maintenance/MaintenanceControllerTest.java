package uk.ac.ceh.gateway.catalogue.maintenance;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.exports.CatalogueExportService;
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProgress;
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProvider;
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProvider.SourceGraph;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.jena.JenaIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.mapserver.MapServerIndexingService;
import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.ceh.gateway.catalogue.indexing.solr.PendingEmbeddingService;
import uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.ADMIN;
import static uk.ac.ceh.gateway.catalogue.controllers.DocumentController.MAINTENANCE_ROLE;

@WithMockCatalogueUser(
    username=ADMIN,
    grantedAuthorities=MAINTENANCE_ROLE
)
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("MaintenanceController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})

public @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class MaintenanceControllerTest extends AbstractMvcTest {
    @MockitoBean DataRepositoryOptimizingService repoService;
    @MockitoBean @Qualifier("solr-index") SolrIndexingService indexService;
    @MockitoBean @Qualifier("jena-index") JenaIndexingService linkingService;
    @MockitoBean @Qualifier("mapserver-index") MapServerIndexingService mapserverService;
    @MockitoBean CatalogueService catalogueService;
    @MockitoBean ProfileService profileService;
    /**
     * Registered even though the "exports" profile is off, so the rendered page can be driven from a
     * populated provider. Left unstubbed they are the profile-off case: Mockito answers
     * {@code sourceGraphs()} with an empty list, so the panel stays off the page.
     */
    @MockitoBean SourceGraphProvider sourceGraphProvider;
    @MockitoBean SourceGraphProgress sourceGraphProgressBean;
    /**
     * Registered although the "vector-search" profile is off, so the rendered page can be driven
     * from real counts. Spring injects it as a present Optional, which is the profile-on case.
     */
    @MockitoBean PendingEmbeddingService embeddingService;
    @Autowired private Configuration configuration;

    private MaintenanceController controller;
    private final String catalogueKey = "eidc";

    @BeforeEach
    @SneakyThrows
    public void createMaintenanceController() {
        // No CatalogueExportService bean under these profiles ("exports" is not active) - Optional.empty()
        // mirrors what Spring itself injects here.
        controller = controllerWith(Optional.empty(), List.of(), Optional.empty());
        // The Spring-wired controller behind the render tests is handed this bean, so give it
        // counts rather than a null, as a bean under "vector-search" would return.
        given(embeddingService.coverage())
            .willReturn(new PendingEmbeddingService.Coverage(1991, 1991, 0, 0));
    }

    private MaintenanceController controllerWith(
        Optional<CatalogueExportService> exportService,
        List<SourceGraphProvider> providers,
        Optional<SourceGraphProgress> progress
    ) {
        return controllerWith(exportService, providers, progress, Optional.empty());
    }

    private MaintenanceController controllerWith(
        Optional<CatalogueExportService> exportService,
        List<SourceGraphProvider> providers,
        Optional<SourceGraphProgress> progress,
        Optional<PendingEmbeddingService> embeddings
    ) {
        return new MaintenanceController(
            repoService, indexService, linkingService, mapserverService, exportService, providers, progress,
            embeddings);
    }

    /** A provider that declares graphs but publishes nothing, which is all the page reads it for. */
    private static SourceGraphProvider declaring(SourceGraph... graphs) {
        return new SourceGraphProvider() {
            @Override public List<SourceGraph> sourceGraphs() {
                return List.of(graphs);
            }
            @Override public Map<String, String> graphs(java.util.Set<String> referencedIris) {
                return Map.of();
            }
        };
    }

    private static SourceGraph graph(String uri, String title) {
        return new SourceGraph(uri, title, "what it holds", List.of(), null);
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("profile", profileService);
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(
                Catalogue.builder()
                    .id(catalogueKey)
                    .title("Env Data Centre")
                    .url("https://example.com")
                    .contactUrl("")
                    .logo("eidc.png")
                    .build()
            );
    }

    @Test
    @SneakyThrows
    void getMaintenancePage() {
        //given
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();

        //when
        mvc.perform(
            get("/maintenance")
            .header("remote-user", ADMIN)
            .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML));
    }

    /**
     * The admin delete capability is only advertised to holders of {@code ROLE_CIG_ADMIN_DELETE}.
     *
     * <p>Note this is decided by group-store membership, not by the granted authorities on the
     * authentication: {@code permission.userCanAdminDelete()} resolves through
     * {@code CrowdPermissionService.userInGroup}, which reads the group store. So the two cases have to be
     * distinguished by <em>username</em> — {@code admin} is in the group, {@code maintenance-only} is
     * not — rather than by varying {@code grantedAuthorities}.</p>
     */
    @Test
    @SneakyThrows
    void showsTheDeleteRecordLinkToAnAdminDeleteHolder() {
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();

        mvc.perform(get("/maintenance").header("remote-user", ADMIN).accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/maintenance/documents/delete")));
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser(
        username = DevelopmentUserStoreConfig.MAINTENANCE_ONLY_USERNAME,
        grantedAuthorities = MAINTENANCE_ROLE
    )
    void hidesTheDeleteRecordLinkFromAMaintenanceOnlyUser() {
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();

        mvc.perform(get("/maintenance")
                .header("remote-user", DevelopmentUserStoreConfig.MAINTENANCE_ONLY_USERNAME)
                .accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("/maintenance/documents/delete"))));
    }

    @Test
    public void checkThatReindexingDelegatesToIndexService() throws DocumentIndexingException {
        //Given
        //Nothing
        //When
        controller.reindexDocuments();

        //Then
        verify(indexService).rebuildIndex();
    }

    @Test
    public void checkThatRecreatingMapfilesDelegatesToMapServerService() throws DocumentIndexingException {
        //Given
        //Nothing
        //When
        controller.recreateMapFiles();

        //Then
        verify(mapserverService).rebuildIndex();
    }

    @Test
    public void checkThatReindexingDelegatesToLinkingService() {
        //Given
        //Nothing

        //When
        controller.reindexLinks();

        //Then
        verify(linkingService).rebuildIndex();
    }

    @Test
    public void checkThatCanOptimizeGitRepository() throws DataRepositoryException {
        //Given
        //Nothing

        //When
        controller.optimizeRepository();

        //Then
        verify(repoService).performOptimization();
    }

    @Test
    public void checkThatCanLoadMaintenancePageWhenThereRepoIsBroken() throws DataRepositoryException {
        //Given
        String errorMessage = "Something has gone wrong";
        when(repoService.getLatestRevision()).thenThrow(new DataRepositoryException(errorMessage));

        //When
        MaintenanceResponse response = controller.loadMaintenancePage();

        //Then
        assertThat("Expected one message", response.getMessages().size(), equalTo(1));
        assertThat("Expected message to exist", response.getMessages().contains(errorMessage));
    }

    @Test
    public void checkThatCanLoadMaintenancePageWhenIndexingIsBroken() throws DocumentIndexingException {
        //Given
        String errorMessage = "Something has gone wrong";
        when(indexService.isIndexEmpty()).thenThrow(new DocumentIndexingException(errorMessage));

        //When
        MaintenanceResponse response = controller.loadMaintenancePage();

        //Then
        assertThat("Expected one message", response.getMessages().size(), equalTo(1));
        assertThat("Expected message to exist", response.getMessages().contains(errorMessage));
    }

    /**
     * dri-one #330: the maintenance page should never fail to load, or accidentally start advertising a
     * feature that isn't wired up, purely because the {@code exports} profile is off.
     */
    @Test
    public void hidesFusekiExportWhenNoCatalogueExportServiceIsConfigured() {
        //When
        MaintenanceResponse response = controller.loadMaintenancePage();

        //Then
        assertThat(response.isExportsAvailable(), equalTo(false));
        assertThat(response.getLastExported(), nullValue());
    }

    @Test
    public void advertisesFusekiExportWhenACatalogueExportServiceIsConfigured() {
        //Given
        CatalogueExportService exportService = mock(CatalogueExportService.class);
        MaintenanceController controllerWithExports =
            controllerWith(Optional.of(exportService), List.of(), Optional.empty());

        //When
        MaintenanceResponse response = controllerWithExports.loadMaintenancePage();

        //Then
        assertThat(response.isExportsAvailable(), equalTo(true));
    }

    @Test
    public void checkThatExportingToFusekiDelegatesToCatalogueExportService() {
        //Given
        CatalogueExportService exportService = mock(CatalogueExportService.class);
        MaintenanceController controllerWithExports =
            controllerWith(Optional.of(exportService), List.of(), Optional.empty());

        //When
        HttpEntity<MaintenanceResponse> response = controllerWithExports.exportToFuseki();

        //Then
        verify(exportService).runExport();
        assertThat(((ResponseEntity<MaintenanceResponse>) response).getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody().getMessages(), hasItem(containsString("Fuseki export")));
    }

    @Test
    public void checkThatFusekiExportFailureIsReportedNotThrown() {
        //Given
        CatalogueExportService exportService = mock(CatalogueExportService.class);
        RestClientResponseException exception = mock(RestClientResponseException.class);
        given(exception.getMessage()).willReturn("Fuseki is down");
        doThrow(exception).when(exportService).runExport();
        MaintenanceController controllerWithExports =
            controllerWith(Optional.of(exportService), List.of(), Optional.empty());

        //When
        HttpEntity<MaintenanceResponse> response = controllerWithExports.exportToFuseki();

        //Then
        assertThat(((ResponseEntity<MaintenanceResponse>) response).getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(response.getBody().getMessages(), hasItem("Fuseki is down"));
    }

    @Test
    public void checkThatFusekiConnectionFailureIsReportedNotThrown() {
        // RestClientResponseException only covers a 4xx/5xx *response*. A refused connection or a
        // timeout is ResourceAccessException - a sibling, not a subclass - and refresh() is
        // @SneakyThrows over IOException/TemplateException. Catching only the response type let
        // all of those escape as a bare 500 instead of reaching the page.
        //Given
        CatalogueExportService exportService = mock(CatalogueExportService.class);
        doThrow(new ResourceAccessException("Connection refused: fuseki:3030"))
            .when(exportService).runExport();
        MaintenanceController controllerWithExports =
            controllerWith(Optional.of(exportService), List.of(), Optional.empty());

        //When
        HttpEntity<MaintenanceResponse> response = controllerWithExports.exportToFuseki();

        //Then
        assertThat(((ResponseEntity<MaintenanceResponse>) response).getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(response.getBody().getMessages(), hasItem(containsString("Connection refused")));
    }

    @Test
    public void checkThatFusekiRenderFailureIsReportedNotThrown() {
        //Given - what a @SneakyThrows IOException out of refresh()/render looks like at this layer
        CatalogueExportService exportService = mock(CatalogueExportService.class);
        doThrow(new RuntimeException("Template rendering failed")).when(exportService).runExport();
        MaintenanceController controllerWithExports =
            controllerWith(Optional.of(exportService), List.of(), Optional.empty());

        //When
        HttpEntity<MaintenanceResponse> response = controllerWithExports.exportToFuseki();

        //Then
        assertThat(((ResponseEntity<MaintenanceResponse>) response).getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(response.getBody().getMessages(), hasItem(containsString("Template rendering failed")));
    }

    @Test
    public void checkThatExportingToFusekiWithoutACatalogueExportServiceIsReportedNotThrown() {
        //Given
        //controller from @BeforeEach has no CatalogueExportService (mirrors a non-"exports" profile)

        //When
        HttpEntity<MaintenanceResponse> response = controller.exportToFuseki();

        //Then
        assertThat(((ResponseEntity<MaintenanceResponse>) response).getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
        assertThat(response.getBody().getMessages(), hasItem(containsString("not available")));
    }

    /**
     * This whole test class is a {@code @SpringBootTest} with no "exports" profile active, so if a plain
     * constructor dependency on {@code CatalogueExportService} ever crept back in, the context would fail
     * to load and every test in this class - not just this one - would fail. This test additionally
     * confirms the page correctly hides the feature it can't offer.
     */
    @Test
    @SneakyThrows
    void hidesFusekiExportButtonWhenExportsProfileIsNotActive() {
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();

        mvc.perform(get("/maintenance").header("remote-user", ADMIN).accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("/maintenance/exports/fuseki"))));
    }

    @Test
    @DisplayName("every declared graph is listed, including one no run has reached")
    void listsDeclaredGraphsWithNoRunsYet() {
        // The state the panel exists to make visible: a graph nothing has tried is not the same as a
        // healthy one, and leaving it off the page would make the two look identical.
        //Given
        MaintenanceController withGraphs = controllerWith(
            Optional.empty(),
            List.of(declaring(graph("https://orcid.org/", "ORCID"), graph("https://ror.org/", "ROR"))),
            Optional.of(new SourceGraphProgress())
        );

        //When
        List<MaintenanceResponse.GraphProgress> rows = withGraphs.loadMaintenancePage().getSourceGraphProgress();

        //Then
        assertThat(rows.stream().map(MaintenanceResponse.GraphProgress::title).toList(),
            equalTo(List.of("ORCID", "ROR")));
        assertThat(rows.getFirst().state(), equalTo(MaintenanceController.NOT_RUN));
        assertThat("counts mean nothing before a run and must not be shown as though they did",
            rows.getFirst().hasCounts(), equalTo(false));
    }

    @Test
    @DisplayName("a graph that is filling reports how far it has got")
    void reportsFillProgress() {
        //Given
        SourceGraphProgress progress = new SourceGraphProgress();
        progress.withheld("https://orcid.org/", 2125, 278, 0);
        MaintenanceController withGraphs = controllerWith(
            Optional.empty(), List.of(declaring(graph("https://orcid.org/", "ORCID"))), Optional.of(progress));

        //When
        MaintenanceResponse.GraphProgress row =
            withGraphs.loadMaintenancePage().getSourceGraphProgress().getFirst();

        //Then
        assertThat(row.state(), equalTo("Filling"));
        assertThat(row.described(), equalTo(1847));
        assertThat(row.entities(), equalTo(2125));
        assertThat(row.outstanding(), equalTo(278));
        assertThat("a first fill is the correct behaviour, not something to act on",
            row.warning(), equalTo(false));
    }

    @Test
    @DisplayName("a graph that has stopped converging is flagged, because it will not fix itself")
    void flagsAGraphThatIsNotConverging() {
        //Given
        SourceGraphProgress progress = new SourceGraphProgress();
        progress.withheld("https://doi.org/", 3340, 139, 0);
        progress.withheld("https://doi.org/", 3340, 139, 0);
        MaintenanceController withGraphs = controllerWith(
            Optional.empty(), List.of(declaring(graph("https://doi.org/", "Crossref"))), Optional.of(progress));

        //When
        MaintenanceResponse.GraphProgress row =
            withGraphs.loadMaintenancePage().getSourceGraphProgress().getFirst();

        //Then
        assertThat(row.state(), equalTo("Not converging"));
        assertThat(row.warning(), equalTo(true));
    }

    /**
     * dri-one #330 again, for the panel rather than the button: the maintenance page must not fail, or
     * advertise a feature that isn't wired up, purely because the {@code exports} profile is off. With
     * no providers there is nothing to describe, and a required {@code List<SourceGraphProvider>}
     * constructor parameter would have stopped the context starting at all.
     */
    @Test
    @DisplayName("nothing is reported when the exports profile is not active")
    void noSourceGraphsWithoutTheExportsProfile() {
        //When - controller from @BeforeEach has no providers and no progress
        MaintenanceResponse response = controller.loadMaintenancePage();

        //Then
        assertThat(response.getSourceGraphProgress(), is(empty()));
    }

    @Test
    @SneakyThrows
    @DisplayName("the panel renders each graph with its state")
    void rendersTheSourceGraphPanel() {
        //Given
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();
        given(sourceGraphProvider.sourceGraphs()).willReturn(List.of(
            graph("https://ror.org/", "ROR"),
            graph("https://doi.org/", "Crossref")
        ));
        given(sourceGraphProgressBean.lastRun()).willReturn(Map.of(
            "https://ror.org/", new SourceGraphProgress.Run(561, 0, SourceGraphProgress.State.PUBLISHED)
        ));

        //When
        String page = mvc.perform(get("/maintenance").header("remote-user", ADMIN).accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        //Then
        assertThat(page, containsString("Source graphs"));
        assertThat(page, containsString("ROR"));
        assertThat(page, containsString("Published"));
        assertThat(page, containsString("561 / 561"));
        assertThat("a declared graph no run has reached is still listed",
            page, containsString(MaintenanceController.NOT_RUN));
    }

    @Test
    @SneakyThrows
    @DisplayName("the panel is absent when no provider declares a graph")
    void hidesTheSourceGraphPanelWithoutProviders() {
        //Given - sourceGraphProvider is unstubbed, so it declares nothing
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();

        //When / Then
        mvc.perform(get("/maintenance").header("remote-user", ADMIN).accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Source graphs"))));
    }

    // ------------------------------------------------------------ embedding coverage panel

    /**
     * Without the "vector-search" profile there is no PendingEmbeddingService at all, and the page
     * must not advertise a semantic search that is not wired up. A required constructor dependency
     * would additionally have stopped every other context from starting.
     */
    @Test
    @DisplayName("nothing is reported when the vector-search profile is not active")
    void noEmbeddingProgressWithoutTheProfile() {
        MaintenanceResponse response = controller.loadMaintenancePage();

        assertThat(response.getEmbeddingProgress(), is(nullValue()));
    }

    @Test
    @SneakyThrows
    @DisplayName("coverage is reported when the profile is active")
    void reportsEmbeddingCoverage() {
        given(embeddingService.coverage())
            .willReturn(new PendingEmbeddingService.Coverage(1847, 1991, 144, 0));
        MaintenanceController withEmbeddings =
            controllerWith(Optional.empty(), List.of(), Optional.empty(), Optional.of(embeddingService));

        MaintenanceResponse.EmbeddingProgress progress =
            withEmbeddings.loadMaintenancePage().getEmbeddingProgress();

        assertThat(progress.embedded(), is(1847L));
        assertThat(progress.total(), is(1991L));
        assertThat(progress.state(), is("Filling"));
    }

    /**
     * Solr being unreachable is reported the same way the indexing checks above report it — as a
     * message, not an exception that takes the whole maintenance page down with it.
     */
    @Test
    @SneakyThrows
    @DisplayName("a Solr failure is reported as a message, leaving the rest of the page intact")
    void solrFailureBecomesAMessage() {
        given(embeddingService.coverage()).willThrow(new SolrServerException("solr down"));
        MaintenanceController withEmbeddings =
            controllerWith(Optional.empty(), List.of(), Optional.empty(), Optional.of(embeddingService));

        MaintenanceResponse response = withEmbeddings.loadMaintenancePage();

        assertThat(response.getEmbeddingProgress(), is(nullValue()));
        assertThat(response.getMessages(), hasItem(containsString("solr down")));
    }

    @Test
    @SneakyThrows
    @DisplayName("the panel renders the coverage and its state")
    void rendersTheEmbeddingPanel() {
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();
        given(embeddingService.coverage())
            .willReturn(new PendingEmbeddingService.Coverage(1847, 1991, 144, 0));

        String page = mvc.perform(get("/maintenance").header("remote-user", ADMIN).accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(page, containsString("Semantic search"));
        assertThat(page, containsString("1847"));
        assertThat(page, containsString("1991"));
        assertThat(page, containsString("Filling"));
    }

    @Test
    @SneakyThrows
    @DisplayName("abandoned records are rendered as a warning")
    void rendersAbandonedRecordsAsAWarning() {
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();
        given(embeddingService.coverage())
            .willReturn(new PendingEmbeddingService.Coverage(1988, 1991, 0, 3));

        String page = mvc.perform(get("/maintenance").header("remote-user", ADMIN).accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(page, containsString("3 records abandoned"));
        assertThat(page, containsString("text-danger"));
    }

    @Test
    @SneakyThrows
    @DisplayName("the panel is absent when the coverage cannot be read")
    void hidesTheEmbeddingPanelWhenCoverageIsUnavailable() {
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();
        given(embeddingService.coverage()).willThrow(new SolrServerException("solr down"));

        mvc.perform(get("/maintenance").header("remote-user", ADMIN).accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Semantic search"))));
    }
}
