package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@Data
@Accessors(chain = true)
@ConvertUsing({
        @Template(called = "/html/search.html.tpl", whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
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
