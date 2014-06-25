package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class SearchResults<T> {
    private Header header;
    private List<T> results;
    private Map<String,List<String>> facets;
    
    @Data
    @Accessors(chain = true)
    public static class Header{
        private long numFound, start, rows;
        private String term;
    }
}
