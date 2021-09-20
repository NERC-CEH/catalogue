package uk.ac.ceh.gateway.catalogue.citation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import java.net.URI;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.BIBTEX_VALUE;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RESEARCH_INFO_SYSTEMS_VALUE;

@Value
@Builder
@ConvertUsing({
    @Template(called="citation/bib.ftlh", whenRequestedAs=BIBTEX_VALUE),
    @Template(called="citation/ris.ftlh", whenRequestedAs=RESEARCH_INFO_SYSTEMS_VALUE)
})
public class Citation {
    List<String> authors;
    String doi, coupled, title, publisher, resourceTypeGeneral;
    Integer year;
    URI bibtex, ris;

    public String getUrl() {
        return "https://doi.org/" + doi;
    }

    @JsonIgnore
    public String getDoiDisplay() {
        return "doi:" + doi;
    }
}
