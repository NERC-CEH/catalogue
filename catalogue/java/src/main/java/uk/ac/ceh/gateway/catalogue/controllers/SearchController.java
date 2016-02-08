package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.search.FacetFilter;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.search.SearchQuery;
import uk.ac.ceh.gateway.catalogue.search.SpatialOperation;

@Controller
@Slf4j
public class SearchController {
    public static final String PAGE_DEFAULT_STRING = "1";
    public static final String ROWS_DEFAULT_STRING = "20";
    public static final String OP_DEFAULT_STRING = "IsWithin";
    
    public static final String TERM_QUERY_PARAM = "term";
    public static final String BBOX_QUERY_PARAM = "bbox";
    public static final String OP_QUERY_PARAM = "op";
    public static final String PAGE_QUERY_PARAM = "page";
    public static final String ROWS_QUERY_PARAM = "rows";
    public static final String FACET_QUERY_PARAM = "facet";
    
    public static final int PAGE_DEFAULT = Integer.parseInt(PAGE_DEFAULT_STRING);
    public static final int ROWS_DEFAULT = Integer.parseInt(ROWS_DEFAULT_STRING);
    
    private final SolrServer solrServer;
    private final GroupStore<CatalogueUser> groupStore;
      
    @Autowired
    public SearchController(SolrServer solrServer, GroupStore<CatalogueUser> groupStore){
        this.solrServer = Objects.requireNonNull(solrServer);
        this.groupStore = Objects.requireNonNull(groupStore);
    }
    
    @RequestMapping(value = "documents",
                    method = RequestMethod.GET)
    public @ResponseBody SearchResults searchDocuments(
            @ActiveUser CatalogueUser user,
            @RequestParam(value = TERM_QUERY_PARAM, defaultValue=SearchQuery.DEFAULT_SEARCH_TERM) String term,
            @RequestParam(value = BBOX_QUERY_PARAM, required = false) String bbox,
            @RequestParam(value = OP_QUERY_PARAM, defaultValue = OP_DEFAULT_STRING) String op,
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = PAGE_DEFAULT_STRING) int page,
            @RequestParam(value = ROWS_QUERY_PARAM, defaultValue = ROWS_DEFAULT_STRING) int rows,
            @RequestParam(value = FACET_QUERY_PARAM, defaultValue = "") List<FacetFilter> facetFilters,
            HttpServletRequest request
    ) throws SolrServerException {
        SearchQuery searchQuery = new SearchQuery(
            request.getRequestURL().toString(),
            user,
            term,
            bbox,
            SpatialOperation.valueOf(op.toUpperCase()),
            page,
            rows,
            facetFilters,
            groupStore
        );
        log.debug("query: {}", searchQuery);
        SearchResults results = new SearchResults(
            solrServer.query(
                searchQuery.build(),
                SolrRequest.METHOD.POST
            ),
            searchQuery
        );
        log.debug("Search Results: {}", results);
        return results;
    }
}