package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.search.DocumentSearchResults;
import uk.ac.ceh.gateway.catalogue.search.SearchQuery;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;

@Controller
public class SearchController {
    private final SolrServer solrServer;  
      
    @Autowired
    public SearchController(SolrServer solrServer){
        this.solrServer = solrServer;
    }
    
    @RequestMapping(value = "documents",
                    method = RequestMethod.GET)
    public @ResponseBody SearchResults searchDocuments(
            @ActiveUser CatalogueUser user,
            @RequestParam(value = "term", defaultValue=SearchQuery.DEFAULT_SEARCH_TERM) String term,
            @RequestParam(value = "bbox", required = false) String bbox,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "rows", defaultValue = "20") int rows,
            @RequestParam(value = "facet", defaultValue = "") List<String> facetFilters,
            HttpServletRequest request
    ) throws SolrServerException {
        SearchQuery searchQuery = new SearchQuery(user, term, bbox, page, rows, facetFilters);
        return new DocumentSearchResults(
            solrServer.query(
                searchQuery.build(),
                SolrRequest.METHOD.POST
            ),
            searchQuery,
            ServletUriComponentsBuilder.fromRequestUri(request)
        );
    }
}