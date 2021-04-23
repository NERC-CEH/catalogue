package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import java.net.URI;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.BIBTEX_VALUE;
import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.RESEARCH_INFO_SYSTEMS_VALUE;

@Value
@Builder
@ConvertUsing({
    @Template(called="citation/bib.tpl", whenRequestedAs=BIBTEX_VALUE),
    @Template(called="citation/ris.tpl", whenRequestedAs=RESEARCH_INFO_SYSTEMS_VALUE)
})
@JsonIgnoreProperties({"doiDisplay"})
public class Citation {
    List<String> authors;
    String doi, coupled, title, publisher, resourceTypeGeneral;
    Integer year;
    URI bibtex, ris;
    
    public String getUrl() {
        return "https://doi.org/" + doi;
    }
    
    public String getDoiDisplay() {
        return "doi:" + doi;
    }
}
