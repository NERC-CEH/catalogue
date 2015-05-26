package uk.ac.ceh.gateway.catalogue.ef;

import java.util.*;
import javax.xml.bind.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.EF_INSPIRE_XML_VALUE;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@XmlRootElement
@XmlType(propOrder = {
    "lifespan",
    "setUpFor",
    "uses"
})
@ConvertUsing({
    @Template(called="html/ema.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="xml/ema.xml.tpl",   whenRequestedAs=EF_INSPIRE_XML_VALUE)
})
public class Activity extends BaseMonitoringType {
    
    private Lifespan lifespan;
    
    private List<Link> 
        setUpFor = new ArrayList<>(),
        uses = new ArrayList<>();
}