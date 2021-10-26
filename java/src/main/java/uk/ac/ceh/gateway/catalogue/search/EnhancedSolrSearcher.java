package uk.ac.ceh.gateway.catalogue.search;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Link;

import java.util.List;

@Slf4j
@Profile("search:enhanced")
@Service
public class EnhancedSolrSearcher extends SolrSearcher {

    public EnhancedSolrSearcher(
        SolrClient solrClient,
        GroupStore<CatalogueUser> groupStore,
        CatalogueService catalogueService,
        FacetFactory facetFactory
    ) {
        super(solrClient, groupStore, catalogueService, facetFactory);
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
        String catalogueKey
    ) {
        val basicResults= super.search(
            endpoint,
            user,
            term,
            bbox,
            spatialOperation,
            page,
            rows,
            facetFilters,
            catalogueKey
        );
        val relatedSearches = relatedSearches(term);
        log.debug(relatedSearches.toString());
        return new SearchResults(basicResults, relatedSearches);
    }

    private List<Link> relatedSearches(String term) {
        return List.of(
          Link.builder().href("https://example.com/carbon").title("carbon").build(),
          Link.builder().href("https://example.com/climate").title("climate").build()
        );
    }
}
