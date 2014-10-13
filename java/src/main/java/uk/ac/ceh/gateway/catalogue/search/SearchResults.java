package uk.ac.ceh.gateway.catalogue.search;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;
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
public class SearchResults {
    private static final Escaper escaper = UrlEscapers.urlFormParameterEscaper();
    private final List<FacetFilter> filters;
    private final UriComponentsBuilder builder;
    
    private final @Getter int page;
    private final @Getter int rows;
    private final @Getter long numFound;
    private final @Getter String term;
    private final @Getter List<String> facetFilters;
    private final @Getter List<DocumentSolrIndex> results;
    private final @Getter List<Facet> facets;
        
    public SearchResults(QueryResponse response, SearchQuery query, UriComponentsBuilder builder) {
        List<DocumentSolrIndex> docs = response.getBeans(DocumentSolrIndex.class);
        
        this.filters = (query.getFacetFilters() != null)? query.getFacetFilters() : Collections.EMPTY_LIST;
        this.builder = builder;
        this.numFound = response.getResults().getNumFound();
        this.term = query.getTermNotDefault();
        this.page = query.getPage();
        this.rows = query.getRows();
        this.results = docs;
        this.facets = getFacets(response);
        this.facetFilters =this.filters.stream().map(FacetFilter::asFormContent).collect(Collectors.toList());
    }
    
    private List<Facet> getFacets(QueryResponse response){
        
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
                String state = getState(field, name);
                return FacetResult.builder()
                    .name(name)
                    .count(count.getCount())
                    .state(state)
                    .url(getUrl(field, name, state))
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    private List<FacetResult> getFacetResults(List<PivotField> pivotFields) {
        return pivotFields.stream()
            .map(pivotField -> {
                String field = pivotField.getField();
                String name = pivotField.getValue().toString();
                String state = getState(field, name);
                return FacetResult.builder()
                    .name(name)
                    .count(pivotField.getCount())
                    .state(state)
                    .url(getUrl(field, name, state))
                    .subFacetResults((pivotField.getPivot() != null)? getFacetResults(pivotField.getPivot()) : Collections.EMPTY_LIST)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private String getState(String facetField, String facetValue) {
        if (filters.contains(new FacetFilter(facetField, facetValue))) {
            return "active";
        } else {
            return "inactive";
        }
    } 
    
    private String getUrl(String field, String value, String state) {
        StringBuilder queryParam = new StringBuilder();
            
        if ( !getTerm().isEmpty()) {
            queryParam.append("term=")
            .append(escaper.escape(getTerm()));
        }
        
        if (state.equals("inactive")) {
            queryParam.append("&facet=")
                .append(field)
                .append("|")
                .append(escaper.escape(value));
        }
        
        if ( !filters.isEmpty()) {
            queryParam.append("&")
                .append(
                    filters.stream()
                        .filter(filter -> !filter.equals(new FacetFilter(field, value)))
                        .filter(filter -> !(filter.getField().equals("sci1") && field.equals("sci0")))
                        .map(FacetFilter::asURIContent)
                        .collect(Collectors.joining("&", "facet=", ""))
                );
        }
        return builder.replaceQuery(queryParam.toString()).build().toUriString();
    }
}