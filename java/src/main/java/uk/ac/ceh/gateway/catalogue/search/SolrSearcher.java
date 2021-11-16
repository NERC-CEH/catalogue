package uk.ac.ceh.gateway.catalogue.search;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Collections;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Profile("search:basic")
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

    @SneakyThrows
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
        String catalogueKey
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
            facetFactory.newInstances(catalogue.getFacetKeys())
        );
        val response = solrClient.query(
            "documents",
            searchQuery.build(),
            POST
        );
        return new SearchResults(response, searchQuery, Collections.emptyList());
    }
}
