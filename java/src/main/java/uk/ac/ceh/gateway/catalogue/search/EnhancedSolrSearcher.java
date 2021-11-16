package uk.ac.ceh.gateway.catalogue.search;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.vocabularies.Keyword;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;
import static uk.ac.ceh.gateway.catalogue.search.SearchQuery.DEFAULT_SEARCH_TERM;
import static uk.ac.ceh.gateway.catalogue.vocabularies.SparqlKeywordVocabulary.COLLECTION;

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
        val relatedSearches = relatedSearches(endpoint, term);
        log.debug(relatedSearches.toString());
        return new SearchResults(basicResults, relatedSearches);
    }

    @SneakyThrows
    private List<Link> relatedSearches(String endpoint, String term) {
        log.debug("related searches - endpoint: {}, term: {}", endpoint, term);
        if (term.equals(DEFAULT_SEARCH_TERM)) {
            return Collections.emptyList();
        }
        try {
            val solrQuery = new SolrQuery(term);
            solrQuery.setParam(CommonParams.DF, "label");
            log.debug("keyword solr query: {}", solrQuery);
            val response = solrClient.query(COLLECTION, solrQuery, POST);
            val possibleKeyword = response.getBeans(Keyword.class).stream().findFirst();
            log.debug("possible keyword? {}", possibleKeyword.isPresent());
            return possibleKeyword
                .map(retriever::retrieve)
                .orElse(Collections.emptyList())
                .stream()
                .map(keyword ->
                    Link.builder()
                        .title(keyword.getLabel())
                        .href(format("%s?term=%s", endpoint, keyword.getLabel()))
                        .build()
                )
                .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Cannot get related searches", ex);
            return Collections.emptyList();
        }
    }
}
