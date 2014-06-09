package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SearchResults<T> {
    private Header header;
    private List<T> results;
    
    @Data
    @Accessors(chain = true)
    public static class Header{
        private long numFound, start, rows;
    }
    
    @Data
    @Accessors(chain = true)
    public static class DocumentSearchResult{
        private UUID id;
        private String title, description;
    }
}
