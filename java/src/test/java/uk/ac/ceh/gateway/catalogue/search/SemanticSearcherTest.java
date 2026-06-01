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

    @BeforeEach
    @SneakyThrows
    void setup() {
        searcher = new SemanticSearcher(embeddingModel, solrClient, groupStore, catalogueService);
        given(queryResponse.getResults()).willReturn(new SolrDocumentList());
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
        assertThat(query).startsWith("{!knn f=vector topK=20}[");
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
}
