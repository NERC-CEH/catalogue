package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocumentSolrIndexGenerator.GeminiDocumentSolrIndex;
import uk.ac.ceh.gateway.catalogue.model.DocumentSearchResults;
import uk.ac.ceh.gateway.catalogue.model.SearchResults;
import uk.ac.ceh.gateway.catalogue.model.SearchResults.Header;

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
            @RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "rows", defaultValue = "20") int rows
    ) throws SolrServerException{
        SolrQuery query = getQuery(term, start, rows);
        return performQuery(query);
    }
    
    private SearchResults<GeminiDocumentSolrIndex> performQuery(SolrQuery query) throws SolrServerException{
        List<GeminiDocumentSolrIndex> results = solrServer.query(query, SolrRequest.METHOD.POST).getBeans(GeminiDocumentSolrIndex.class);
        Header header = new SearchResults.Header()
                .setNumFound(results.size())
                .setStart(query.getStart())
                .setRows(query.getRows());        
        return new DocumentSearchResults()
                .setHeader(header)
                .setResults(results);
    }
    
    private SolrQuery getQuery(String term, int start, int rows){
        SolrQuery query = new SolrQuery()
                .setQuery(term)
                .setStart(start)
                .setRows(rows);
        return query;
    }
    
}