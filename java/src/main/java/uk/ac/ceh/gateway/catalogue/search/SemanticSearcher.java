package uk.ac.ceh.gateway.catalogue.search;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.io.IOException;
import java.util.Collections;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Slf4j
@Service
// Gated on the profile rather than @ConditionalOnBean(EmbeddingModel.class): that annotation is
// only reliable on auto-configuration classes. On a component-scanned @Service it is evaluated
// while scanning, before Spring AI registers the embedding model, so it never matched and this
// bean never loaded — silently, because every consumer injects it as an Optional.
@Profile("vector-search")
public class SemanticSearcher {

    /**
     * Ceiling on the KNN candidate list. {@code topK} bounds the whole result set rather than one
     * page — Solr returns at most {@code topK} documents and {@code numFound} never exceeds it — so
     * setting it to the page size broke paging twice over: {@code start} landed at or past the end of
     * the candidate list, emptying every page after the first, and {@code numFound} was held to a
     * single page, which suppressed {@link SearchResults}' next-page link so the second page was
     * never even offered.
     * <p>
     * The ceiling is deliberately independent of the requested page. Deriving it from {@code page}
     * would make {@code numFound} grow as the user paged, so the reported result count would change
     * underneath them. Matches beyond this limit are unreachable, which is inherent to a top-K
     * vector query; {@code numFound} reflects the limit, so no page is offered that cannot be filled.
     */
    static final int KNN_CANDIDATE_LIMIT = 200;

    private final EmbeddingModel embeddingModel;
    private final SolrClient solrClient;
    private final GroupStore<CatalogueUser> groupStore;
    private final CatalogueService catalogueService;

    public SemanticSearcher(
            EmbeddingModel embeddingModel,
            SolrClient solrClient,
            GroupStore<CatalogueUser> groupStore,
            CatalogueService catalogueService
    ) {
        this.embeddingModel = embeddingModel;
        this.solrClient = solrClient;
        this.groupStore = groupStore;
        this.catalogueService = catalogueService;
        log.info("Creating");
    }

    public SearchResults search(
            String endpoint,
            CatalogueUser user,
            String term,
            String bbox,
            SpatialOperation spatialOperation,
            int page,
            int rows,
            String catalogueKey
    ) {
        val catalogue = catalogueService.retrieve(catalogueKey);

        float[] vec = embedQuery(term, endpoint, catalogueKey);
        String vectorStr = buildVectorString(vec);

        int knnTopK = Math.max(rows, KNN_CANDIDATE_LIMIT);

        SolrQuery query = new SolrQuery("{!knn f=vector topK=" + knnTopK + "}" + vectorStr)
                .setStart((page - 1) * rows)
                .setRows(rows);

        if (bbox != null) {
            query.addFilterQuery(String.format("locations:\"%s(ENVELOPE(%s))\"",
                    spatialOperation.getOperation(), bbox));
        }
        applyVisibilityFilters(query, user, catalogue.getId(), catalogueKey);
        SolrVisibilityFilter.applyCatalogueScope(query, catalogue.getId());

        log.debug("Semantic query: {}", query);

        QueryResponse response;
        try {
            response = solrClient.query("documents", query, POST);
        } catch (SolrServerException | IOException e) {
            throw SearchBackendFailure.unavailable("semantic search", endpoint, catalogueKey, e);
        }

        // Construct a minimal SearchQuery for SearchResults pagination URLs
        val searchQuery = new SearchQuery(
                endpoint, user, term, bbox, spatialOperation,
                page, rows,
                Collections.emptyList(),
                groupStore, catalogue,
                Collections.emptyList(),
                null, SolrQuery.ORDER.asc,
                true
        );
        return new SearchResults(response, searchQuery, Collections.emptyList());
    }

    private void applyVisibilityFilters(SolrQuery query, CatalogueUser user, String catalogueId, String catalogueKey) {
        SolrVisibilityFilter.apply(query, user, groupStore, catalogueId, catalogueKey);
    }

    /**
     * The embedding call reaches Amazon Bedrock, whose failures — throttling, expired credentials,
     * a rejected payload — all arrive as unchecked AWS SDK exceptions. Spring AI 2.0 no longer
     * offers a common wrapper for them (its {@code TransientAiException} went with the 1.x retry
     * module), so the catch is deliberately wide: anything from this call is an upstream problem,
     * and letting it escape produced a bare 500 with a stack trace and no indication of which
     * search or catalogue failed.
     */
    private float[] embedQuery(String term, String endpoint, String catalogueKey) {
        try {
            return embeddingModel.embed(term);
        } catch (RuntimeException e) {
            throw SearchBackendFailure.unavailable("embedding for semantic search", endpoint, catalogueKey, e);
        }
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
