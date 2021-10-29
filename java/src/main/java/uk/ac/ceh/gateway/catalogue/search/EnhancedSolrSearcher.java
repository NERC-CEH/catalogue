package uk.ac.ceh.gateway.catalogue.search;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.vocabularies.Keyword;

import java.util.Collections;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Slf4j
@Profile("search:enhanced")
@Service
public class EnhancedSolrSearcher extends SolrSearcher {
    private final BroaderNarrowerRetriever retriever;

    public EnhancedSolrSearcher(
        SolrClient solrClient,
        GroupStore<CatalogueUser> groupStore,
        CatalogueService catalogueService,
        FacetFactory facetFactory,
        BroaderNarrowerRetriever retriever
    ) {
        super(solrClient, groupStore, catalogueService, facetFactory);
        this.retriever = retriever;
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

    @SneakyThrows
    private List<Link> relatedSearches(String term) {
        try {
            val solrQuery = new SolrQuery(term);
            val response = solrClient.query("vocabularies", solrQuery, POST);
            val possibleKeyword = response.getBeans(Keyword.class).stream().findFirst();
            return possibleKeyword
                .map(retriever::retrieve)
                .orElse(Collections.emptyList());
        } catch (Exception ex) {
            log.error("Cannot get related searches", ex);
            return Collections.emptyList();
        }
    }
}
