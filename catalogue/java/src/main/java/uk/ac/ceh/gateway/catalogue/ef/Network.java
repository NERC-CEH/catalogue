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
    "legalBackground",
    "observingCapabilities",
    "supersedes",
    "supersededBy",
    "narrowerThan",
    "broaderThan",
    "involvedIn",
    "contains"
})
@ConvertUsing({
    @Template(called="html/ef.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="xml/emn.xml.tpl",   whenRequestedAs=EF_INSPIRE_XML_VALUE)
})
public class Network extends BaseMonitoringType {
    private List<Link> 
        legalBackground = new ArrayList<>(),
        involvedIn = new ArrayList<>(),
        supersedes = new ArrayList<>(),
        supersededBy = new ArrayList<>();
    
    private List<Link.TimedLink>
        broaderThan = new ArrayList<>(),
        narrowerThan = new ArrayList<>(),
        contains = new ArrayList<>();
    
    @XmlElement(name = "observingCapability")
    private List<ObservingCapability> observingCapabilities = new ArrayList<>();
}