package uk.ac.ceh.gateway.catalogue.gemini;

import java.net.URI;
import java.util.List;
import lombok.Value;
import lombok.experimental.Builder;

/**
 *
 * @author cjohn
 */
@Value
@Builder
public class Citation {
    private final List<String> authors;
    private final String doi, coupled, title, publisher;
    private final Integer year;
    private final URI bibtex, ris;
    
    public String getUrl() {
        return "http://dx.doi.org/" + doi;
    }
    
    public String getDoiDisplay() {
        return "doi:" + doi;
    }
}
