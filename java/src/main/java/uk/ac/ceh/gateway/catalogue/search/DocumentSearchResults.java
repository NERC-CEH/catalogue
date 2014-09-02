package uk.ac.ceh.gateway.catalogue.search;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
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
public class DocumentSearchResults extends SearchResults<DocumentSolrIndex> {
        
    public DocumentSearchResults(QueryResponse response, List<FacetFilter> facetFilters) {
        List<DocumentSolrIndex> docs = response.getBeans(DocumentSolrIndex.class);
        SolrDocumentList results = response.getResults();
        NamedList responseHeader = response.getResponseHeader();
        
        this.setNumFound(results.getNumFound());
        this.setTerm((responseHeader.get("q") != null)? responseHeader.get("q").toString() : "");
        this.setStart(response.getResults().getStart());
        this.setRows(Long.parseLong(responseHeader.get("rows").toString(), 10));
        this.setFacetFilters(facetFilters.stream().map(FacetFilter::asFormContent).collect(Collectors.toList()));
        this.setResults(docs);
        this.setFacets(getFacets(response, facetFilters));

    }
    
    private List<Facet> getFacets(QueryResponse response, List<FacetFilter> facetFilters){
        String filterQuery = facetFilters.stream().map(f -> f.asURIContent()).collect(Collectors.joining("&", "facet=", null));
        
        
        List<Facet> toReturn = new ArrayList<>();
        response.getFacetFields().stream().forEach((facetField) -> {
            
            List<FacetResult> facetResults = new ArrayList<>();
                       
            facetField.getValues().stream().forEach((facetResult) -> {
                facetResults.add(FacetResult.builder()
                    .name(facetResult.getName())
                    .count(facetResult.getCount())
                    .state(getState(facetField.getName(), facetResult.getName(), facetFilters))
                    .url(new StringBuilder("/documents?term=").append(this.getTerm()).append("&").append(filterQuery).toString())
                    .build()
                );
            });
            
            toReturn.add(Facet
                .builder()
                .fieldName(facetField.getName())
                .displayName(SearchQuery.FACET_FIELDS.get(facetField.getName()))
                .results(facetResults)
                .build()
            );
        });
        List<FacetResult> pivots = new ArrayList<>();
        response.getFacetPivot().get("sci0,sci1").stream().forEach((pivotField) -> {
            List<FacetResult> subPivots = new ArrayList<>();
            pivotField.getPivot().stream().forEach((sub) -> {
                subPivots.add(FacetResult.builder()
                    .name(sub.getValue().toString())
                    .count(sub.getCount())
                    .build());
            });
            pivots.add(FacetResult
                .builder()
                .name(pivotField.getValue().toString())
                .count(pivotField.getCount())
                .subFacetResults(subPivots)
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
    
    private String getState(String facetField, String facetValue, List<FacetFilter> facetFilters) {
        if (facetFilters.contains(new FacetFilter(facetField, facetValue))) {
            return "active";
        } else {
            return "inactive";
        }
    }
    
    
}