package uk.ac.ceh.gateway.catalogue.search;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Collections;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Slf4j
@Service
@ConditionalOnBean(EmbeddingModel.class)
public class HybridSearcher {

    // Same field weights as SearchQuery.build() — keeps BM25 sub-query consistent with direct search
    static final String QF =
            "title^5 description^2 keyword^5 lineage familyName altTitle " +
            "resourceIdentifier identifier supplementalDescription supplementalName " +
            "infrastructureCapabilities^2 keywordsParameters^5 observedPropertyTitle^10 " +
            "observedPropertyValue^5 operatingPeriod objectives^2 responsibleParties document_text^1";

    static final int RRF_K = 60;
    static final int KNN_MULTIPLIER = 5;
    static final int KNN_MAX_TOP_K = 200;

    private final EmbeddingModel embeddingModel;
    private final SolrClient solrClient;
    private final GroupStore<CatalogueUser> groupStore;
    private final CatalogueService catalogueService;

    public HybridSearcher(
            EmbeddingModel embeddingModel,
            SolrClient solrClient,
            GroupStore<CatalogueUser> groupStore,
            CatalogueService catalogueService
    ) {
        this.embeddingModel = embeddingModel;
        this.solrClient = solrClient;
        this.groupStore = groupStore;
        this.catalogueService = catalogueService;
        log.info("Creating HybridSearcher");
    }

    @SneakyThrows
    public SearchResults search(
            String endpoint,
            CatalogueUser user,
            String term,
            int page,
            int rows,
            String catalogueKey
    ) {
        val catalogue = catalogueService.retrieve(catalogueKey);

        float[] vec = embeddingModel.embed(term);
        String vectorStr = buildVectorString(vec);

        // topK must exceed rows so the KNN side supplies enough distinct candidates for fusion
        int knnTopK = Math.min(rows * KNN_MULTIPLIER, KNN_MAX_TOP_K);

        SolrQuery query = new SolrQuery("{!rrf queries=$bm25q,$knnq k=" + RRF_K + "}");
        query.setParam("bm25q", "{!edismax qf=\"" + QF + "\"}" + term);
        query.setParam("knnq",  "{!knn f=vector topK=" + knnTopK + "}" + vectorStr);
        query.setStart((page - 1) * rows);
        query.setRows(rows);

        SolrVisibilityFilter.apply(query, user, groupStore, catalogue.getId(), catalogueKey);
        if (!CatalogueService.ALL_CATALOGUES_ID.equals(catalogueKey)) {
            query.addFilterQuery("{!term f=catalogue}" + catalogueKey);
        }

        log.debug("Hybrid RRF query: {}", query);

        val response = solrClient.query("documents", query, POST);

        // Minimal SearchQuery used only for SearchResults pagination URL generation
        val searchQuery = new SearchQuery(
                endpoint, user, term, null, SpatialOperation.ISWITHIN,
                page, rows,
                Collections.emptyList(),
                groupStore, catalogue,
                Collections.emptyList(),
                null, SolrQuery.ORDER.asc
        );
        return new SearchResults(response, searchQuery, Collections.emptyList());
    }

    private String buildVectorString(float[] vec) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vec.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(vec[i]);
        }
        return sb.append(']').toString();
    }
}
