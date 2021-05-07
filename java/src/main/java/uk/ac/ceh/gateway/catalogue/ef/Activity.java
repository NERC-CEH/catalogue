package uk.ac.ceh.gateway.catalogue.ef;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.EF_INSPIRE_XML_VALUE;

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
    @Template(called="html/ef.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="xml/ema.ftlx", whenRequestedAs=EF_INSPIRE_XML_VALUE)
})
public class Activity extends BaseMonitoringType {

    private Lifespan lifespan;

    private List<Link>
        setUpFor = new ArrayList<>(),
        uses = new ArrayList<>();
}
