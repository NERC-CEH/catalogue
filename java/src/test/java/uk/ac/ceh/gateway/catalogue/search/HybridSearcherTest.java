package uk.ac.ceh.gateway.catalogue.search;

import lombok.SneakyThrows;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HybridSearcherTest {

    @Mock private EmbeddingModel embeddingModel;
    @Mock private SolrClient solrClient;
    @Mock private GroupStore<CatalogueUser> groupStore;
    @Mock private CatalogueService catalogueService;
    @Mock private QueryResponse queryResponse;

    private HybridSearcher searcher;

    private final Catalogue eidc = Catalogue.builder()
        .id("eidc").title("EIDC").url("").contactUrl("").logo("").build();
    private final Catalogue all = Catalogue.builder()
        .id("all").title("All").url("").contactUrl("").logo("").build();

    @BeforeEach
    @SneakyThrows
    void setup() {
        searcher = new HybridSearcher(embeddingModel, solrClient, groupStore, catalogueService);
        given(queryResponse.getResults()).willReturn(new SolrDocumentList());
        given(queryResponse.getBeans(any())).willReturn(List.of());
        given(solrClient.query(eq("documents"), any(SolrParams.class), any())).willReturn(queryResponse);
    }

    @Test
    @SneakyThrows
    void combinerParamsAreSetForRrf() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f, 0.2f, 0.3f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER, "river flow", 1, 20, "eidc");

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(eq("documents"), captor.capture(), any());
        SolrParams p = captor.getValue();

        // Native Solr Reciprocal Rank Fusion combiner (CombinerParams), not a {!rrf} parser
        assertThat(p.getBool("combiner")).isTrue();
        assertThat(p.get("combiner.algorithm")).isEqualTo("rrf");
        assertThat(p.get("combiner.rrf.k")).isEqualTo("60");
        assertThat(p.getParams("combiner.query")).containsExactlyInAnyOrder("bm25q", "knnq");
    }

    @Test
    @SneakyThrows
    void bm25qContainsEdismaxAndTerm() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER, "peat bog carbon", 1, 20, "eidc");

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(eq("documents"), captor.capture(), any());

        String bm25q = captor.getValue().get("json.queries.bm25q");
        assertThat(bm25q).startsWith("{!edismax");
        assertThat(bm25q).contains("peat bog carbon");
        assertThat(bm25q).contains("title^5");
    }

    @Test
    @SneakyThrows
    void knnqContainsKnnAndVector() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f, 0.2f, 0.3f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER, "river flow", 1, 20, "eidc");

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(eq("documents"), captor.capture(), any());

        String knnq = captor.getValue().get("json.queries.knnq");
        assertThat(knnq).startsWith("{!knn f=vector topK=");
        assertThat(knnq).contains("0.1").contains("0.2").contains("0.3");
        assertThat(knnq).endsWith("]");
    }

    @Test
    @SneakyThrows
    void knnTopKIsRowsTimesMultiplier() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER, "nitrogen", 1, 20, "eidc");

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(eq("documents"), captor.capture(), any());

        assertThat(captor.getValue().get("json.queries.knnq")).contains("topK=100");
    }

    @Test
    @SneakyThrows
    void knnTopKIsCappedAtMax() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        // rows=50 → 50×5=250 which exceeds cap of 200
        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER, "nitrogen", 1, 50, "eidc");

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(eq("documents"), captor.capture(), any());

        assertThat(captor.getValue().get("json.queries.knnq")).contains("topK=200");
    }

    @Test
    @SneakyThrows
    void publicUserVisibilityFiltersApplied() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER, "flood", 1, 20, "eidc");

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(eq("documents"), captor.capture(), any());

        String[] fqs = captor.getValue().getParams("fq");
        assertThat(fqs).anyMatch(fq -> fq.contains("state") && fq.contains("published"));
        assertThat(fqs).anyMatch(fq -> fq.contains("view") && fq.contains("public"));
    }

    @Test
    @SneakyThrows
    void catalogueFilterAppliedForSpecificCatalogue() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER, "soil", 1, 20, "eidc");

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(eq("documents"), captor.capture(), any());

        assertThat(captor.getValue().getParams("fq"))
                .anyMatch(fq -> fq.contains("catalogue") && fq.contains("eidc"));
    }

    @Test
    @SneakyThrows
    void noCatalogueFilterForAllCatalogues() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve(CatalogueService.ALL_CATALOGUES_ID)).willReturn(all);

        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER,
                "soil", 1, 20, CatalogueService.ALL_CATALOGUES_ID);

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(eq("documents"), captor.capture(), any());

        String[] fqs = captor.getValue().getParams("fq");
        if (fqs != null) {
            assertThat(fqs).noneMatch(fq -> fq.contains("{!term f=catalogue}"));
        }
    }

    @Test
    @SneakyThrows
    void paginationStartIsCalculatedCorrectly() {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(catalogueService.retrieve("eidc")).willReturn(eidc);

        // page=2, rows=10 → start should be 10
        searcher.search("http://example.com", CatalogueUser.PUBLIC_USER, "flood", 2, 10, "eidc");

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(eq("documents"), captor.capture(), any());

        assertThat(captor.getValue().getInt("start", -1)).isEqualTo(10);
    }
}
