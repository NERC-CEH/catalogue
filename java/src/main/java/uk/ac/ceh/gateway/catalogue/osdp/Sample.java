package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/osdp/sample.html.tpl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class Sample extends ResearchArtifact {
    private String medium, geometry;
}