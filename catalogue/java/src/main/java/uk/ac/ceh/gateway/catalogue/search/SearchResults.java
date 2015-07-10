package uk.ac.ceh.gateway.catalogue.search;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndex;

/**
 * A search results object for documents
 * @author jcoop, cjohn
 */
@ConvertUsing({
    @Template(called = "/html/search.html.tpl", whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
@AllArgsConstructor
@Slf4j
public class SearchResults {
    private final QueryResponse response;
    private final SearchQuery query;
    
    public long getNumFound() {
        return response.getResults().getNumFound();
    }
    
    public String getTerm() {
        return query.getTermNotDefault();
    }
    
    public int getPage() {
        return query.getPage();
    }
    
    public int getRows() {
        return query.getRows();
    }
    
    /**
     * Return a link to the previous page as long as we are not currently on the
     * first page.
     * @return url to the previous page or null.
     */
    public String getPrevPage() {
        if(getPage() != 1) {
            return query.withPage(getPage() - 1).toUrl();
        }
        else {
            return null;
        }
    }
    
    /**
     * Return a link to the next page as long as there is a page to go to.
     * @return A link to the next page if there is one to go to.
     */
    public String getNextPage() {
        if(getNumFound() > getPage() * getRows()) {
            return query.withPage(getPage() + 1).toUrl();
        }
        else {
            return null;
        }
    }
    
    /**
     * Return a link to a search which is not applying the applied bounding box
     * filter
     * @return url to a search without the bbox component
     */
    public String getWithoutBbox() {
        if(query.getBbox() != null) {
            return query.withBbox(null).toUrl();
        }
        else {
            return null;   
        }
    }
    
    public String getIntersectingBbox() {
        if(query.getBbox() != null && query.getSpatialOperation() != SpatialOperation.INTERSECTS) {
            return query.withSpatialOperation(SpatialOperation.INTERSECTS).toUrl();
        }
        else {
            return null;
        }
    }
    
    public String getWithinBbox() {
        if(query.getBbox() != null && query.getSpatialOperation() != SpatialOperation.ISWITHIN) {
            return query.withSpatialOperation(SpatialOperation.ISWITHIN).toUrl();
        }
        else {
            return null;
        }
    }
    
    public String getUrl() {
        return query.toUrl();
    }
    
    public List<SolrIndex> getResults() {
        return response.getBeans(SolrIndex.class);
    }
    
    public List<Facet> getFacets(){
        List<Facet> facets = query.getFacets();
       
        facets.forEach((facet) -> {
            FacetField facetField = response.getFacetField(facet.getFieldName());
            if (facetField != null) {
                if (facet.isHierarchical()) {
                    facet.getResults().addAll(getHierarchicalFacetResults(facetField, "0/"));
                } else {
                    facet.getResults().addAll(getFacetResults(facetField));
                }
            }
        });
        
        return facets;
    }

    private List<FacetResult> getFacetResults(FacetField facetField) {
        return facetField.getValues().stream()
            .map(count -> {
                String field = facetField.getName();
                String name = count.getName();
                FacetFilter filter = new FacetFilter(field, name);
                boolean active = query.containsFacetFilter(filter);
                return FacetResult.builder()
                    .name(name)
                    .count(count.getCount())
                    .active(active)
                    .url(((active) ? query.withoutFacetFilter(filter) : query.withFacetFilter(filter)).toUrl())
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    private List<FacetResult> getHierarchicalFacetResults(FacetField facetField, String prefix) {
        return facetField.getValues().stream()
            .filter(c -> c.getName().startsWith(prefix))
            .map(c -> getFacetResultFromCount(c))
            .collect(Collectors.toList());
    }
    
    private FacetResult getFacetResultFromCount(FacetField.Count count) {
        String name = count.getName();
        FacetFilter filter = new FacetFilter(count.getFacetField().getName(), name);
        boolean active = query.containsFacetFilter(filter);
        return FacetResult.builder()
            .name(getName(name))
            .count(count.getCount())
            .active(active)
            .url(((active) ? query.withoutFacetFilter(filter) : query.withFacetFilter(filter)).toUrl())
            .subFacetResults(getHierarchicalFacetResults(count.getFacetField(), getChildName(name)))
            .build();
    }
    
    private String getChildName(String name) {
        int i = name.indexOf("/");  
        Integer child = Integer.parseInt(name.substring(0, i)) + 1;
        return child + name.substring(i);
    }
    
    private String getName(String name) {
        int last = name.length() - 1;
        int i = name.lastIndexOf("/", last - 1) + 1;
        String toReturn = name.substring(i, last);
        log.debug("name: {}, length: {}, last: {}, i: {}, return: {}", name, name.length(), last, i, toReturn);
        return toReturn;
    }
}