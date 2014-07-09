package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocumentSolrIndexGenerator.GeminiDocumentSolrIndex;
import uk.ac.ceh.gateway.catalogue.model.DocumentSearchResults;
import uk.ac.ceh.gateway.catalogue.model.SearchResults;
import uk.ac.ceh.gateway.catalogue.model.SearchResults.Facet;
import uk.ac.ceh.gateway.catalogue.model.SearchResults.Header;

@Controller
public class SearchController {
    
    private final SolrServer solrServer;
    
    private static final Map<String, String> FACET_FIELDS;
    protected static final String DEFAULT_SEARCH_TERM = "*";
    static
    {
        FACET_FIELDS = new HashMap<>();
        FACET_FIELDS.put("resourceType", "Resource type");
        FACET_FIELDS.put("isOgl", "OGL license");
    }
    
    @Autowired
    public SearchController(SolrServer solrServer){
        this.solrServer = solrServer;
    }
    
    @RequestMapping(value = "documents",
                    method = RequestMethod.GET)
    public @ResponseBody SearchResults searchDocuments(
            @RequestParam(value = "term", defaultValue=DEFAULT_SEARCH_TERM) String term,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "rows", defaultValue = "20") int rows,
            @RequestParam(value = "facet", defaultValue = "") List<String> facets
    ) throws SolrServerException{
        SolrQuery query = getQuery(term, start, rows, facets);
        return performQuery(term, query);
    }
    
    private SearchResults<GeminiDocumentSolrIndex> performQuery(String term, SolrQuery query) throws SolrServerException{
        QueryResponse response = solrServer.query(query, SolrRequest.METHOD.POST);
        List<GeminiDocumentSolrIndex> results = response.getBeans(GeminiDocumentSolrIndex.class);
        Header header = new SearchResults.Header()
                .setNumFound(response.getResults().getNumFound())
                .setTerm(term)
                .setStart(query.getStart())
                .setRows(query.getRows());  
        return new DocumentSearchResults()
                .setHeader(header)
                .setResults(results)
                .setFacets(getFacets(response));
    }
    
    private List<Facet> getFacets(QueryResponse response){
        
        List<SearchResults.Facet> toReturn = new ArrayList<>();
        
        for(FacetField facetField : response.getFacetFields()){
            List<SearchResults.FacetResult> facetResults = new ArrayList<>();
            for(Count count : facetField.getValues()){
                facetResults.add(SearchResults.FacetResult
                        .builder()
                        .name(count.getName())
                        .count(count.getCount())
                        .build()
                );
            }
            toReturn.add(Facet
                    .builder()
                    .fieldName(facetField.getName())
                    .displayName(FACET_FIELDS.get(facetField.getName()))
                    .results(facetResults)
                    .build()
            );
        }
        return toReturn;
    }
    
    private SolrQuery getQuery(String term, int start, int rows, List<String> facets){
        SolrQuery query = new SolrQuery()
                .setQuery(term)
                .setStart(start)
                .setRows(rows);
        setFacetQueries(query, facets);
        setFacetFields(query);
        setSortOrder(query, term);
        return query;
    }
    
    private void setFacetQueries(SolrQuery query, List<String> facets){
        if(facets != null && facets.size() > 0){
            for(String facet : facets){
                String[] facetParts = facet.split("\\|");
                query.addFacetQuery(facetParts[0] + ":" + facetParts[1]);
            }
        }
    }
    
    private void setFacetFields(SolrQuery query){
        query.setFacet(true);
        query.setFacetMinCount(1);
        for(Entry<String, String> entry : FACET_FIELDS.entrySet()){
            query.addFacetField(entry.getKey());
        }
    }
    
    private void setSortOrder(SolrQuery query, String term){
        Random randomGenerator = new Random(System.currentTimeMillis());
        String randomDynamicFieldName = "random" + randomGenerator.nextInt();
        if(DEFAULT_SEARCH_TERM.equals(term)){
            query.setSort(randomDynamicFieldName, ORDER.asc);
        }
    }
    
}