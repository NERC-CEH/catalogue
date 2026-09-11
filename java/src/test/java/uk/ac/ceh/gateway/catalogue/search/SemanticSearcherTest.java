package uk.ac.ceh.gateway.catalogue.search;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ai.embedding.EmbeddingModel;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.ExternalResourceFailureException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SemanticSearcherTest {

    @Mock private EmbeddingModel embeddingModel;
    @Mock private SolrClient solrClient;
    @Mock private GroupStore<CatalogueUser> groupStore;
    @Mock private CatalogueService catalogueService;
    @Mock private QueryResponse queryResponse;

    private SemanticSearcher searcher;

    private final Catalogue eidc = Catalogue.builder()
        .id("eidc").title("EIDC").url("").contactUrl("").logo("").build();
    private final Catalogue all = Catalogue.builder()
        .id("all").title("All").url("").contactUrl("").logo("").build();

    private final SolrDocumentList solrResults = new SolrDocumentList();

    @BeforeEach
    @SneakyThrows
    void setup() {
        searcher = new SemanticSearcher(embeddingModel, solrClient, groupStore, catalogueService);
        given(queryResponse.getResults()).willReturn(solrResults);
        given(queryResponse.getBeans(any())).willReturn(List.of());
        given(solrClient.query(eq("documents"), any(SolrParams.class), any())).willReturn(queryResponse);
    }

    @Test
    @SneakyThrows
    void knnQueryIsFormattedCorrectly() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f, 0.2f, 0.3f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "river flow", null, SpatialOperation.ISWITHIN, 1, 20, "eidc");

        ArgumentCaptor<SolrParams> paramsCaptor = ArgumentCaptor.forClass(SolrParams.class);
        org.mockito.Mockito.verify(solrClient).query(eq("documents"), paramsCaptor.capture(), any());

        String query = paramsCaptor.getValue().get("q");
        assertThat(query).startsWith("{!knn f=vector topK=" + SemanticSearcher.KNN_CANDIDATE_LIMIT + "}[");
        assertThat(query).contains("0.1").contains("0.2").contains("0.3");
    }

    @Test
    @SneakyThrows
    void spatialFilterIsAddedWhenBboxProvided() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "nitrogen", "-5.0,2.0,60.0,50.0", SpatialOperation.ISWITHIN, 1, 20, "eidc");

        ArgumentCaptor<SolrParams> paramsCaptor = ArgumentCaptor.forClass(SolrParams.class);
        org.mockito.Mockito.verify(solrClient).query(eq("documents"), paramsCaptor.capture(), any());

        String[] filterQueries = paramsCaptor.getValue().getParams("fq");
        assertThat(filterQueries).anyMatch(fq -> fq.contains("locations") && fq.contains("iswithin"));
    }

    @Test
    @SneakyThrows
    void noSpatialFilterWhenBboxIsNull() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "nitrogen", null, SpatialOperation.ISWITHIN, 1, 20, "eidc");

        ArgumentCaptor<SolrParams> paramsCaptor = ArgumentCaptor.forClass(SolrParams.class);
        org.mockito.Mockito.verify(solrClient).query(eq("documents"), paramsCaptor.capture(), any());

        String[] filterQueries = paramsCaptor.getValue().getParams("fq");
        if (filterQueries != null) {
            assertThat(filterQueries).noneMatch(fq -> fq.contains("locations"));
        }
    }

    @Test
    @SneakyThrows
    void catalogueFilterAppliedForSpecificCatalogue() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "flood", null, SpatialOperation.ISWITHIN, 1, 20, "eidc");

        ArgumentCaptor<SolrParams> paramsCaptor = ArgumentCaptor.forClass(SolrParams.class);
        org.mockito.Mockito.verify(solrClient).query(eq("documents"), paramsCaptor.capture(), any());

        String[] filterQueries = paramsCaptor.getValue().getParams("fq");
        assertThat(filterQueries).anyMatch(fq -> fq.contains("catalogue") && fq.contains("eidc"));
    }

    @Test
    @SneakyThrows
    void noCatalogueFilterForAllCatalogues() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve(CatalogueService.ALL_CATALOGUES_ID)).willReturn(all);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "flood", null, SpatialOperation.ISWITHIN, 1, 20, CatalogueService.ALL_CATALOGUES_ID);

        ArgumentCaptor<SolrParams> paramsCaptor = ArgumentCaptor.forClass(SolrParams.class);
        org.mockito.Mockito.verify(solrClient).query(eq("documents"), paramsCaptor.capture(), any());

        String[] filterQueries = paramsCaptor.getValue().getParams("fq");
        if (filterQueries != null) {
            assertThat(filterQueries).noneMatch(fq -> fq.contains("{!term f=catalogue}"));
        }
    }

    @Test
    @SneakyThrows
    void publicUserPermissionFilterApplied() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "flood", null, SpatialOperation.ISWITHIN, 1, 20, "eidc");

        ArgumentCaptor<SolrParams> paramsCaptor = ArgumentCaptor.forClass(SolrParams.class);
        org.mockito.Mockito.verify(solrClient).query(eq("documents"), paramsCaptor.capture(), any());

        String[] filterQueries = paramsCaptor.getValue().getParams("fq");
        assertThat(filterQueries).anyMatch(fq -> fq.contains("state") && fq.contains("published"));
        assertThat(filterQueries).anyMatch(fq -> fq.contains("view") && fq.contains("public"));
    }

    // --- Pagination (topK bounds the candidate set, not the page) ---

    @Test
    @DisplayName("topK bounds the candidate set rather than the requested page")
    @SneakyThrows
    void topKIsNotCappedToOnePage() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "river flow", null, SpatialOperation.ISWITHIN, 1, 20, "eidc");

        assertThat(capturedParams().get("q"))
            .startsWith("{!knn f=vector topK=" + SemanticSearcher.KNN_CANDIDATE_LIMIT + "}[");
    }

    @Test
    @DisplayName("A later page is offset within the candidate set instead of past its end")
    @SneakyThrows
    void laterPageStaysInsideTheCandidateSet() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "river flow", null, SpatialOperation.ISWITHIN, 3, 20, "eidc");

        SolrParams params = capturedParams();
        assertThat(params.getInt("start")).isEqualTo(40);
        assertThat(params.getInt("rows")).isEqualTo(20);
        // The invariant that broke, asserted against the topK actually sent to Solr rather than the
        // constant: the requested window has to fall inside the candidate list. With topK == rows,
        // start (40) sat beyond the 20 candidates that existed and the page came back empty.
        assertThat(params.getInt("start") + params.getInt("rows"))
            .isLessThanOrEqualTo(topKOf(params.get("q")));
    }

    @Test
    @DisplayName("numFound reports the whole match count, so a next page is offered")
    @SneakyThrows
    void nextPageIsOfferedWhenMatchesExceedOnePage() {
        solrResults.setNumFound(75);
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        SearchResults results = searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "river flow", null, SpatialOperation.ISWITHIN, 1, 20, "eidc");

        assertThat(results.getNumFound()).isEqualTo(75);
        // Covers the consequence rather than the cause: Solr is mocked here, so numFound is stubbed
        // and this cannot observe topK capping it. What it does pin is the wiring — a match count
        // beyond one page reaches SearchResults and produces a next-page link — which is the
        // behaviour the old topK denied by holding numFound down to a single page.
        assertThat(results.getNextPage()).isNotNull();
    }

    private int topKOf(String query) {
        var matcher = java.util.regex.Pattern.compile("topK=(\\d+)").matcher(query);
        assertThat(matcher.find()).as("topK in %s", query).isTrue();
        return Integer.parseInt(matcher.group(1));
    }

    @SneakyThrows
    private SolrParams capturedParams() {
        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        org.mockito.Mockito.verify(solrClient).query(eq("documents"), captor.capture(), any());
        return captor.getValue();
    }

    // --- Catalogue scope and upstream failures ---

    @Test
    @DisplayName("Records shared into a catalogue are in scope via catalogue_view")
    @SneakyThrows
    void catalogueViewIsIncludedInTheScopeFilter() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "river flow", null, SpatialOperation.ISWITHIN, 1, 20, "eidc");

        // A record shared into a catalogue carries it in catalogue_view, not catalogue. Filtering on
        // catalogue alone hid exactly those records from semantic search while ordinary search
        // still returned them.
        assertThat(capturedParams().getParams("fq"))
            .anyMatch(fq -> fq.contains("catalogue:eidc") && fq.contains("catalogue_view:eidc"));
    }

    @Test
    @DisplayName("An unreachable Solr becomes a 502-mapped failure, not a leaked stack trace")
    @SneakyThrows
    @MockitoSettings(strictness = Strictness.LENIENT)  // the shared Solr stubbing is not reached
    void solrFailureIsReportedAsUpstreamFailure() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);
        given(solrClient.query(eq("documents"), any(SolrParams.class), any()))
            .willThrow(new SolrServerException("http://solr:8983/solr refused the connection"));

        assertThatThrownBy(() -> searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "river flow", null, SpatialOperation.ISWITHIN, 1, 20, "eidc"))
            .isInstanceOf(ExternalResourceFailureException.class)
            // The message reaches the response body, so it must not carry SolrJ's own text — that
            // includes the Solr base URL, on an endpoint reachable anonymously.
            .hasMessageNotContaining("solr:8983")
            .hasCauseInstanceOf(SolrServerException.class);
    }

    @Test
    @DisplayName("A Bedrock failure becomes the same upstream failure rather than a bare 500")
    @SneakyThrows
    @MockitoSettings(strictness = Strictness.LENIENT)  // the shared Solr stubbing is not reached
    void embeddingFailureIsReportedAsUpstreamFailure() {
        given(catalogueService.retrieve("eidc")).willReturn(eidc);
        given(embeddingModel.embed(any(String.class)))
            .willThrow(new RuntimeException("ThrottlingException: rate exceeded, requestId=abc123"));

        assertThatThrownBy(() -> searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
            "river flow", null, SpatialOperation.ISWITHIN, 1, 20, "eidc"))
            .isInstanceOf(ExternalResourceFailureException.class)
            .hasMessageNotContaining("requestId")
            .hasRootCauseMessage("ThrottlingException: rate exceeded, requestId=abc123");
    }

    /**
     * SemanticSearcher builds a SearchQuery purely so SearchResults can render the next/prev
     * page links. SearchQuery had no notion of semantic mode, so toUrl() could not emit it and
     * every pagination link silently dropped semantic=true. Following one put the user on a BM25
     * page 2: for a term where keyword search matched far fewer records than the KNN candidate
     * set, the requested offset was past the end and the page came back empty. On staging that
     * showed up as "semantic search returns zero results" with no error anywhere.
     */
    @Test
    @SneakyThrows
    @DisplayName("The next page link keeps the search in semantic mode")
    void nextPageLinkStaysSemantic() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);
        solrResults.setNumFound(84);

        val results = searcher.search("http://example.com/eidc/documents", CatalogueUser.PUBLIC_USER,
            "nitrogen deposition", null, SpatialOperation.ISWITHIN, 1, 20, "eidc");

        assertThat(results.getNextPage())
            .as("page 2 reverts to keyword search without this, and can come back empty")
            .contains("semantic=true");
    }

    @Test
    @SneakyThrows
    @DisplayName("The previous page link keeps the search in semantic mode")
    void prevPageLinkStaysSemantic() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);
        solrResults.setNumFound(84);

        val results = searcher.search("http://example.com/eidc/documents", CatalogueUser.PUBLIC_USER,
            "nitrogen deposition", null, SpatialOperation.ISWITHIN, 2, 20, "eidc");

        assertThat(results.getPrevPage()).contains("semantic=true");
    }
}
