package uk.ac.ceh.gateway.catalogue.datacite;

import lombok.Builder;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.DATACITE_XML_VALUE;

@Builder
@Value
@ConvertUsing(
    @Template(called = "/datacite/datacite.ftlx", whenRequestedAs = DATACITE_XML_VALUE)
)
public class DataciteResponse {
    GeminiDocument doc;
    String resourceType;
    String doi;
}
