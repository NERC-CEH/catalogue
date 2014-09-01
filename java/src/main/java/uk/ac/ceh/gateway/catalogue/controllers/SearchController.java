package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.indexing.MetadataDocumentSolrIndexGenerator.DocumentSolrIndex;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentSearchResults;
import uk.ac.ceh.gateway.catalogue.model.SearchResults;
import uk.ac.ceh.gateway.catalogue.model.SearchResults.Facet;
import uk.ac.ceh.gateway.catalogue.model.SearchResults.Header;

@Controller
public class SearchController {
    
    private final SolrServer solrServer;
    
    private static final String RANDOM_DYNAMIC_FIELD_NAME = "random";
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
            @ActiveUser CatalogueUser user,
            @RequestParam(value = "term", defaultValue=DEFAULT_SEARCH_TERM) String term,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "rows", defaultValue = "20") int rows,
            @RequestParam(value = "facet", defaultValue = "") List<String> facetFilters
    ) throws SolrServerException{
        SolrQuery query = getQuery(user, term, start, rows, facetFilters);
        return performQuery(term, facetFilters, query);
    }
    
    private SearchResults<DocumentSolrIndex> performQuery(String term, List<String> facetFilters, SolrQuery query) throws SolrServerException{
        QueryResponse response = solrServer.query(query, SolrRequest.METHOD.POST);
        List<DocumentSolrIndex> results = response.getBeans(DocumentSolrIndex.class);
        Header header = new SearchResults.Header()
                .setNumFound(response.getResults().getNumFound())
                .setTerm(term)
                .setStart(query.getStart())
                .setRows(query.getRows())
                .setFacetFilters(facetFilters);
        return new DocumentSearchResults()
                .setHeader(header)
                .setResults(results)
                .setFacets(getFacets(response));
    }
    
    private List<Facet> getFacets(QueryResponse response){
        List<SearchResults.Facet> toReturn = new ArrayList<>();
        response.getFacetFields().stream().forEach((facetField) -> {
            List<SearchResults.FacetResult> facetResults = new ArrayList<>();
            facetField.getValues().stream().forEach((count) -> {
                facetResults.add(SearchResults.FacetResult
                    .builder()
                    .name(count.getName())
                    .count(count.getCount())
                    .build()
                );
            });
            toReturn.add(Facet
                .builder()
                .fieldName(facetField.getName())
                .displayName(FACET_FIELDS.get(facetField.getName()))
                .results(facetResults)
                .build()
            );
        });
        List<SearchResults.FacetResult> pivots = new ArrayList<>();
        response.getFacetPivot().get("sci0,sci1").stream().forEach((pivotField) -> {
            List<SearchResults.FacetResult> subPivots = new ArrayList<>();
            pivotField.getPivot().stream().forEach((sub) -> {
                subPivots.add(SearchResults.FacetResult.builder()
                    .name(sub.getValue().toString())
                    .count(sub.getCount())
                    .build());
            });
            pivots.add(SearchResults.FacetResult
                .builder()
                .name(pivotField.getValue().toString())
                .count(pivotField.getCount())
                .facetResults(subPivots)
                .build()
            );
        });
        toReturn.add(Facet.builder()
            .fieldName("sci0")
            .displayName("Science Area")
            .results(pivots)
            .build());
        return toReturn;
    }
    
    private SolrQuery getQuery(CatalogueUser user, String term, int start, int rows, List<String> facetFilters){
        SolrQuery query = new SolrQuery()
                .setQuery(term)
                .setParam("defType", "edismax")
                .setParam("qf", "title^5 description")
                .setStart(start)
                .setRows(rows);
        setRecordVisibility(query, user);
        setFacetFilters(query, facetFilters);
        setFacetFields(query);
        setSortOrder(query, term);
        return query;
    }
    
    private void setRecordVisibility(SolrQuery query, CatalogueUser user) {
        if (user.isPublic()) {
            query.addFilterQuery("state:public");
        } else {
            // TODO block access to records for everyone until fixed permissions 
            query.addFilterQuery("state:public");
        }
    }
    
    private void setFacetFilters(SolrQuery query, List<String> facetFilters){
        String delimiter = "|";
        if(facetFilters != null && facetFilters.size() > 0){
            for(String facetFilter : facetFilters){
                if(StringUtils.countOccurrencesOf(facetFilter, delimiter) == 1){
                    String[] facetFilterParts = facetFilter.split("\\" + delimiter);
                    query.addFilterQuery("{!term f=" +facetFilterParts[0] + "}" + facetFilterParts[1]);
                }else{
                    throw new IllegalArgumentException(String.format("This is an invalid facet filter: %s. It should contain one argument delimiter of the type '|'", facetFilter));
                }
            }
        }
    }
    
    private void setFacetFields(SolrQuery query){
        query.setFacet(true);
        query.setFacetMinCount(1);
        query.setFacetSort("index");
        FACET_FIELDS.entrySet().stream().forEach((entry) -> {
            query.addFacetField(entry.getKey());
        });
        query.addFacetPivotField("sci0,sci1");
    }
    
    private void setSortOrder(SolrQuery query, String term){
        if(DEFAULT_SEARCH_TERM.equals(term)){
            query.setSort(getRandomFieldName(), ORDER.asc);
        }
    }
    
    private String getRandomFieldName(){
        Random randomGenerator = new Random(getRandomSeed());
        return RANDOM_DYNAMIC_FIELD_NAME + randomGenerator.nextInt();
    }
    
    private int getRandomSeed(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

}