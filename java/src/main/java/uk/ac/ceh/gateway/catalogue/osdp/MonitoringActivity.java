package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/osdp/monitoringActivity.html.tpl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class MonitoringActivity extends AbstractMetadataDocument {
    private TimePeriod temporalExtent;
    private List<Parameter> parametersMeasured;
}