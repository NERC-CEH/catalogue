package uk.ac.ceh.gateway.catalogue.search;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.Value;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.util.StringUtils;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@Value
public class SearchQuery {
    public static final String DEFAULT_SEARCH_TERM = "*";
    private static final String RANDOM_DYNAMIC_FIELD_NAME = "random";
    public static final Map<String, String> FACET_FIELDS;
    static
    {
        FACET_FIELDS = new HashMap<>();
        FACET_FIELDS.put("resourceType", "Resource type");
        FACET_FIELDS.put("isOgl", "OGL license");
    }
    
    private CatalogueUser user; 
    String term;
    int start, rows;
    List<FacetFilter> facetFilters;
      
    public SolrQuery build(){
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
    
    private void setFacetFilters(SolrQuery query, List<FacetFilter> facetFilters){
        if(facetFilters != null){
            facetFilters.stream().forEach((filter) -> {
                query.addFilterQuery(
                    
                );
            });
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
            query.setSort(getRandomFieldName(), SolrQuery.ORDER.asc);
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