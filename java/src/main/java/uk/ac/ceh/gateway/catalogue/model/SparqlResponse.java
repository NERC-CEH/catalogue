package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@ConvertUsing({
    @Template(called="html/sparql.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Data
public class SparqlResponse {
    private String error, result, query;
}
