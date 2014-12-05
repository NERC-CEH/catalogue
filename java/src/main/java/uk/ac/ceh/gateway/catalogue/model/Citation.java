package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.URI;
import java.util.List;
import lombok.Value;
import lombok.experimental.Builder;
import uk.ac.ceh.gateway.catalogue.config.WebConfig;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

/**
 *
 * @author cjohn
 */
@Value
@Builder
@ConvertUsing({
    @Template(called="citation/bib.tpl", whenRequestedAs=WebConfig.BIBTEX_VALUE),
    @Template(called="citation/ris.tpl", whenRequestedAs=WebConfig.RESEARCH_INFO_SYSTEMS_VALUE)
})
@JsonIgnoreProperties({"doiDisplay"})
public class Citation {
    private final List<String> authors;
    private final String doi, coupled, title, publisher;
    private final Integer year;
    private final URI bibtex, ris;
    
    public String getUrl() {
        return "http://doi.org/" + doi;
    }
    
    public String getDoiDisplay() {
        return "doi:" + doi;
    }
}
