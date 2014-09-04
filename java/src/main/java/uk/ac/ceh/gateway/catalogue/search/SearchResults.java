package uk.ac.ceh.gateway.catalogue.search;

import java.util.List;
import lombok.Data;

@Data
public abstract class SearchResults<T> {
    private int start, rows;
    private long numFound;
    private String term;
    private List<String> facetFilters;
    private List<T> results;
    private List<Facet> facets;   
}