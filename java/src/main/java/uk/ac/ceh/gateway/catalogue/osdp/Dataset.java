package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/osdp/dataset.html.tpl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class Dataset extends ResearchArtifact {
    private List<Parameter> parametersMeasured;
    private String format, version;
}