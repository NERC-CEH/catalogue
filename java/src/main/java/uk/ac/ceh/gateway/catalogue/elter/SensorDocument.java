package uk.ac.ceh.gateway.catalogue.elter;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/elter/sensor.html.tpl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class SensorDocument extends AbstractMetadataDocument {

    private String shortName;
    private String serialNumber;
    private String documentation;
    private String manufacturer;
    private String manufacturerName;
    private String manufacturerWebsite;
    private List<Map<String, String>> defaultParameters;
    private ProcessType processType;
}