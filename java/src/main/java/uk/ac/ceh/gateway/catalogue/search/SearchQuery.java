package uk.ac.ceh.gateway.catalogue.search;

import static com.google.common.base.Strings.nullToEmpty;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@Value
@Slf4j
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
    
    private final CatalogueUser user; 
    private final String term;
    private final int start, rows;
    private final List<FacetFilter> facetFilters;
    
    public SearchQuery(CatalogueUser user, String term, int start, int rows, List<String> facetFilters) {
        log.debug("facet filter strings: {}", facetFilters);
        this.user = user;
        this.term = term;
        this.start = start;
        this.rows = rows;
        this.facetFilters = facetFilters.stream()
            .filter(filter -> !nullToEmpty(filter).isEmpty())
            .map(filter -> new FacetFilter(filter))
            .collect(Collectors.toList());
        log.debug("processed filters: {}", this.facetFilters);
    }
      
    public SolrQuery build(){
        SolrQuery query = new SolrQuery()
                .setQuery(term)
                .setParam("defType", "edismax")
                .setParam("qf", "title^5 description")
                .setStart(start)
                .setRows(rows);
        setSpatialFilter(query);
        setRecordVisibility(query);
        setFacetFilters(query);
        setFacetFields(query);
        setSortOrder(query);
        return query;
    }
    
    public String getTermNotDefault() {
        return (DEFAULT_SEARCH_TERM.equals(term))? "" : term;
    }
    
    private void setSpatialFilter(SolrQuery query) {
        //if(bbox != null) {
            query.addFilterQuery(
                    "locations:\"isWithin(-19.03379 43.27549 10.23379 63.67446)\"");
        //}
    }
    private void setRecordVisibility(SolrQuery query) {
        if (user.isPublic()) {
            query.addFilterQuery("{!term f=state}public");
        } else {
            // TODO block access to records for everyone until fixed permissions 
            query.addFilterQuery("{!term f=state}public");
        }
    }
    
    private void setFacetFilters(SolrQuery query){
        if(facetFilters != null){
            facetFilters.stream().forEach((filter) -> {
                query.addFilterQuery(filter.asSolrFilterQuery());
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
    
    private void setSortOrder(SolrQuery query){
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