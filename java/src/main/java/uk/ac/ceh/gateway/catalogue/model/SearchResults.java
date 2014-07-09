package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Builder;

@Data
@Accessors(chain = true)
public abstract class SearchResults<T> {
    private Header header;
    private List<T> results;
    private List<Facet> facets;
    
    @Data
    @Accessors(chain = true)
    public static class Header{
        private long numFound, start, rows;
        private String term;
        private List<String> facetQueries;
    }
    
    @Data
    @Accessors(chain = true)
    @Builder
    public static class Facet{
        private String fieldName, displayName;
        private List<FacetResult> results;
    }
    
    @Data
    @Accessors(chain = true)
    @Builder
    public static class FacetResult{
        private String name;
        private long count;
    }
}
