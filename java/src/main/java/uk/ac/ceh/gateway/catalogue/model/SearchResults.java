package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class SearchResults<T> {
    private Header header;
    private List<T> results;
    
    @Data
    @Accessors(chain = true)
    public static class Header{
        private long numFound, start, rows;
        private String term;
    }
}
