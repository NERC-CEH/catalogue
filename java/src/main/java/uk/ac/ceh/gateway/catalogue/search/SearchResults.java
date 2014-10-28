package uk.ac.ceh.gateway.catalogue.search;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.indexing.MetadataDocumentSolrIndexGenerator.DocumentSolrIndex;

/**
 * A search results object for documents
 * @author jcoop, cjohn
 */
@ConvertUsing({
    @Template(called = "/html/search.html.tpl", whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
@AllArgsConstructor
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
    public String getWithoutBBox() {
        if(query.getBbox() != null) {
            return query.withBbox(null).toUrl();
        }
        else {
            return null;   
        }
    }
    
    public List<DocumentSolrIndex> getResults() {
        return response.getBeans(DocumentSolrIndex.class);
    }
    
    public List<Facet> getFacets(){        
        List<Facet> toReturn = response.getFacetFields().stream().map((facetField) -> {
            return Facet.builder()
                .fieldName(facetField.getName())
                .displayName(SearchQuery.FACET_FIELDS.get(facetField.getName()))
                .results(getFacetResults(facetField))
                .build();
        }).collect(Collectors.toList());
        
        toReturn.add(Facet.builder()
            .fieldName("sci0")
            .displayName("Science Area")
            .results(getFacetResults(response.getFacetPivot().get("sci0,sci1")))
            .build());
            
        return toReturn;
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
    
    private List<FacetResult> getFacetResults(List<PivotField> pivotFields) {
        return pivotFields.stream()
            .map(pivotField -> {
                String field = pivotField.getField();
                String name = pivotField.getValue().toString();
                FacetFilter filter = new FacetFilter(field, name);
                boolean active = query.containsFacetFilter(filter);

                return FacetResult.builder()
                    .name(name)
                    .count(pivotField.getCount())
                    .active(active)
                    .url(((active) ? query.withoutFacetFilter(filter) : query.withFacetFilter(filter)).toUrl())
                    .subFacetResults((pivotField.getPivot() != null)? getFacetResults(pivotField.getPivot()) : Collections.EMPTY_LIST)
                    .build();
            })
            .collect(Collectors.toList());
    }

}