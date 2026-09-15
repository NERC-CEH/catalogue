package uk.ac.ceh.gateway.catalogue.search;

import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Profile("search-basic")
@Service
public class SolrSearcher implements Searcher {
    final SolrClient solrClient;
    private final GroupStore<CatalogueUser> groupStore;
    private final CatalogueService catalogueService;
    private final FacetFactory facetFactory;

    public SolrSearcher(
        SolrClient solrClient,
        GroupStore<CatalogueUser> groupStore,
        CatalogueService catalogueService,
        FacetFactory facetFactory
    ) {
        this.solrClient = solrClient;
        this.groupStore = groupStore;
        this.catalogueService = catalogueService;
        this.facetFactory = facetFactory;
    }

    @Override
    public SearchResults search(
        String endpoint,
        CatalogueUser user,
        String term,
        String bbox,
        SpatialOperation spatialOperation,
        int page,
        int rows,
        List<FacetFilter> facetFilters,
        String catalogueKey,
        String sortField,
        SolrQuery.ORDER sortOrder
    ) {
        val catalogue = catalogueService.retrieve(catalogueKey);

        val searchQuery = new SearchQuery(
            endpoint,
            user,
            term,
            bbox,
            spatialOperation,
            page,
            rows,
            facetFilters,
            groupStore,
            catalogue,
            facetFactory.newInstances(catalogue.getFacetKeys()),
            sortField,
            sortOrder
        );
        QueryResponse response;
        try {
            response = solrClient.query(
                "documents",
                searchQuery.build(),
                POST
            );
        } catch (SolrServerException | IOException e) {
            throw SearchBackendFailure.unavailable("basic search", endpoint, catalogueKey, e);
        }
        return new SearchResults(response, searchQuery, Collections.emptyList());
    }
}
