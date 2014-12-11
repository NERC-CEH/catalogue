package uk.ac.ceh.gateway.catalogue.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import javax.validation.constraints.NotNull;
import lombok.Value;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.web.util.UriComponentsBuilder;
import static uk.ac.ceh.gateway.catalogue.controllers.SearchController.BBOX_QUERY_PARAM;
import static uk.ac.ceh.gateway.catalogue.controllers.SearchController.FACET_QUERY_PARAM;
import static uk.ac.ceh.gateway.catalogue.controllers.SearchController.OP_QUERY_PARAM;
import static uk.ac.ceh.gateway.catalogue.controllers.SearchController.PAGE_DEFAULT;
import static uk.ac.ceh.gateway.catalogue.controllers.SearchController.PAGE_QUERY_PARAM;
import static uk.ac.ceh.gateway.catalogue.controllers.SearchController.ROWS_DEFAULT;
import static uk.ac.ceh.gateway.catalogue.controllers.SearchController.ROWS_QUERY_PARAM;
import static uk.ac.ceh.gateway.catalogue.controllers.SearchController.TERM_QUERY_PARAM;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@Value
@Slf4j
public class SearchQuery {
    public static final String DEFAULT_SEARCH_TERM = "*";
    private static final String RANDOM_DYNAMIC_FIELD_NAME = "random";
    private final List<Facet> facets = Arrays.asList(
        Facet.builder().fieldName("topic").displayName("Topic").hierarchical(true).build(),
        Facet.builder().fieldName("resourceType").displayName("Resource type").hierarchical(false).build(),
        Facet.builder().fieldName("isOgl").displayName("OGL license").hierarchical(false).build()
    );
    
    private final String endpoint;
    private final CatalogueUser user; 
    private final @NotNull String term;
    private final String bbox;
    private final SpatialOperation spatialOperation;
    private final @Wither int page;
    private final int rows;
    private final @NotNull List<FacetFilter> facetFilters;
      
    public SolrQuery build(){
        SolrQuery query = new SolrQuery()
                .setQuery(term)
                .setParam("defType", "edismax")
                .setParam("qf", "title^5 description")
                .setStart((page-1)*rows)
                .setRows(rows);
        setSpatialFilter(query);
        setRecordVisibility(query);
        setFacetFilters(query);
        setFacetFields(query);
        setSortOrder(query);
        log.debug("search query: {}", query);
        return query;
    }
    
    public String getTermNotDefault() {
        return (DEFAULT_SEARCH_TERM.equals(term))? "" : term;
    }
    
    /**
     * Generate a search query with a new bbox value. This will fundamentally
     * change the search query so we will jump back to page one.
     * @param newBbox the new bbox value or null to remove bbox filtering
     * @return A new search query if the new bbox value would result in a search change
     */
    public SearchQuery withBbox(String newBbox) {
        if ( (bbox == null && newBbox != null) || (bbox !=null && !bbox.equals(newBbox)) ) {
            return new SearchQuery(endpoint, user, term, newBbox, spatialOperation, PAGE_DEFAULT, rows, facetFilters);
        }
        else {
            return this;
        }
    }
    
    /**
     * Generate a search query with a new spatial operation. Changing a spatial
     * operation fundamentally changes the search query which means that we should
     * jump back to page 1.
     * @param newSpatialOperation the new spatial operation
     * @return a new query if spatial operation differs to the one set.
     */
    public SearchQuery withSpatialOperation(SpatialOperation newSpatialOperation) {
        if ( !spatialOperation.equals(newSpatialOperation) ) {
            return new SearchQuery(endpoint, user, term, bbox, newSpatialOperation, PAGE_DEFAULT, rows, facetFilters);
        }
        else {
            return this;
        }
    }
    
    /**
     * Create a clone of this search query but apply the additional facet filter
     * 
     * The logic of this method has been designed to match that of lomboks 
     * @Wither methods.
     * 
     * If the filter has already been applied, just return this search query
     * @param filter to ensure is present
     * @return a new search query or this one if no change is needed
     */
    public SearchQuery withFacetFilter(FacetFilter filter) {
        if (!containsFacetFilter(filter)) {
            List<FacetFilter> newFacetFilters = new ArrayList<>(facetFilters);
            newFacetFilters.add(filter);
            return new SearchQuery(endpoint, user, term, bbox, spatialOperation, PAGE_DEFAULT, rows, newFacetFilters);
        }
        else {
            return this;
        }
    }
    
    /**
     * Create a clone of this search query be ensure that the given facet filter
     * is not applied.
     * 
     * If the filter is not applied, just return this search query
     * @param filter to ensure is missing
     * @return a new search query or this one if no change is needed
     */
    public SearchQuery withoutFacetFilter(FacetFilter filter) {
        if(containsFacetFilter(filter) ) {
            List<FacetFilter> newFacetFilters = new ArrayList<>(facetFilters);
            newFacetFilters.remove(filter);
            return new SearchQuery(endpoint, user, term, bbox, spatialOperation, PAGE_DEFAULT, rows, newFacetFilters);
        }
        else {
            return this;
        }
    }
    
    /**
     * Check to see if the given facet filter is being applied by this search 
     * query.
     * @param filter to see if it is being applied
     * @return true if it is false if it isn't
     */
    public boolean containsFacetFilter(FacetFilter filter) {
        return facetFilters.contains(filter);
    }
    
    /**
     * @return the url to call to perform this solr query. 
     */
    public String toUrl() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpoint);
        if(PAGE_DEFAULT != page) {
            builder.queryParam(PAGE_QUERY_PARAM, page);
        }
        
        if(ROWS_DEFAULT != rows) {
            builder.queryParam(ROWS_QUERY_PARAM, rows);
        }
        
        if(!DEFAULT_SEARCH_TERM.equals(term)) {
            builder.queryParam(TERM_QUERY_PARAM, term);
        }
        
        if(bbox != null) {
            builder.queryParam(BBOX_QUERY_PARAM, bbox);
            builder.queryParam(OP_QUERY_PARAM, spatialOperation.getOperation());
        }
        
        if(!facetFilters.isEmpty()) {
            facetFilters.forEach((f)-> builder.queryParam(FACET_QUERY_PARAM, f.asURIContent()));
        }
        
        return builder.build().toUriString();
    }
    
    private void setSpatialFilter(SolrQuery query) {
        if(bbox != null) {
            validateBBox(bbox);
            query.addFilterQuery(String.format("locations:\"%s(%s)\"", spatialOperation.getOperation(), bbox.replace(',', ' ')));
        }
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
        facetFilters.stream().forEach((filter) -> {
            query.addFilterQuery(filter.asSolrFilterQuery());
        });
    }
    
    private void setFacetFields(SolrQuery query){
        query.setFacet(true);
        query.setFacetMinCount(1);
        query.setFacetSort("index");
        facets.stream().forEach((facet) -> {
            query.addFacetField(facet.getFieldName());
        });
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
    
    private void validateBBox(String bbox) {
        //Validate that the bbox is in the format minx,miny,maxx,maxxy
        String[] bboxParts = bbox.split(",");
        if(bboxParts.length != 4) {
            throw new IllegalArgumentException("The bbox must be in the form minx,miny,maxx,maxy");
        }
        for(String bboxPart :bboxParts) {
            try {
                Double.parseDouble(bboxPart);
            }
            catch(NumberFormatException ex) {
                throw new IllegalArgumentException("The bbox must be in the form minx,miny,maxx,maxy", ex);
            }
        }
    }
}