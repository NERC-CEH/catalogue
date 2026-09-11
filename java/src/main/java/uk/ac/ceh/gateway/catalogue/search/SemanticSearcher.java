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
import java.util.List;

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
    private final FacetFactory facetFactory;

    public SemanticSearcher(
            EmbeddingModel embeddingModel,
            SolrClient solrClient,
            GroupStore<CatalogueUser> groupStore,
            CatalogueService catalogueService,
            FacetFactory facetFactory
    ) {
        this.embeddingModel = embeddingModel;
        this.solrClient = solrClient;
        this.groupStore = groupStore;
        this.catalogueService = catalogueService;
        this.facetFactory = facetFactory;
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
            List<FacetFilter> facetFilters,
            String catalogueKey
    ) {
        val catalogue = catalogueService.retrieve(catalogueKey);
        val facets = facetFactory.newInstances(catalogue.getFacetKeys());

        // Built before the embedding call so that an unknown facet field is rejected without
        // first paying Bedrock to embed a query that is going to 400 anyway.
        val searchQuery = new SearchQuery(
                endpoint, user, term, bbox, spatialOperation,
                page, rows,
                facetFilters,
                groupStore, catalogue,
                facets,
                null, SolrQuery.ORDER.asc,
                true
        );

        SolrQuery query = new SolrQuery();
        searchQuery.applyFacets(query);

        if (bbox != null) {
            query.addFilterQuery(String.format("locations:\"%s(ENVELOPE(%s))\"",
                    spatialOperation.getOperation(), bbox));
        }
        applyVisibilityFilters(query, user, catalogue.getId(), catalogueKey);
        SolrVisibilityFilter.applyCatalogueScope(query, catalogue.getId());

        float[] vec = embedQuery(term, endpoint, catalogueKey);

        int knnTopK = Math.max(rows, KNN_CANDIDATE_LIMIT);
        String facetTags = facetFilters.isEmpty() ? "" : searchQuery.facetTags();
        query.setQuery(knnParser(knnTopK, facetTags) + buildVectorString(vec))
                .setStart((page - 1) * rows)
                .setRows(rows);

        log.debug("Semantic query: {}", query);

        QueryResponse response;
        try {
            response = solrClient.query("documents", query, POST);
        } catch (SolrServerException | IOException e) {
            throw SearchBackendFailure.unavailable("semantic search", endpoint, catalogueKey, e);
        }

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


    /**
     * Solr treats every {@code fq} as an implicit pre-filter when knn is the main query, so the
     * candidate list would be the top-K drawn from within the faceted subset. That yields better
     * matches but collapses the facet counts: every candidate already carries the selected value,
     * the alternatives count zero and {@code facet.mincount=1} drops them, so the panel dead-ends
     * after a single click. Excluding the facet tags leaves them as ordinary filters over the
     * top-K, which is how keyword search behaves and what {@code {!ex=...}} counting expects.
     * <p>
     * Only the facet filters are tagged. Visibility and catalogue scope stay untagged, and so stay
     * pre-filters -- otherwise the candidate budget would be spent on records the user cannot see
     * and then thrown away.
     */
    private String knnParser(int topK, String facetTags) {
        // No facet filter means no tagged fq to exclude, so the parameter is left off entirely
        // rather than naming tags that are not present.
        String excludeTags = facetTags.isBlank() ? "" : " excludeTags=" + facetTags;
        return "{!knn f=vector topK=" + topK + excludeTags + "}";
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
