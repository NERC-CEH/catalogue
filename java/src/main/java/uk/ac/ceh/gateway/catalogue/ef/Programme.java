package uk.ac.ceh.gateway.catalogue.ef;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.EF_INSPIRE_XML_VALUE;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@XmlRootElement
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@XmlType(propOrder = {
    "legalBackground",
    "observingCapabilities",
    "supersedes",
    "supersededBy",
    "narrowerThan",
    "broaderThan",
    "lifespan",
    "reportToLegalAct",
    "triggers"
})
@ConvertUsing({
    @Template(called="html/emp.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="xml/emp.xml.tpl",   whenRequestedAs=EF_INSPIRE_XML_VALUE)
})
public class Programme extends BaseMonitoringType {
    
    private Lifespan lifespan;
    
    private List<Link> 
        legalBackground = new ArrayList<>(),
        reportToLegalAct = new ArrayList<>(),
        supersedes = new ArrayList<>(),
        supersededBy = new ArrayList<>(),
        triggers = new ArrayList<>();
    
    private List<Link.TimedLink> 
        broaderThan  = new ArrayList<>(),
        narrowerThan = new ArrayList<>();
    
    @XmlElement(name = "observingCapability")
    private List<ObservingCapability> observingCapabilities  = new ArrayList<>();
}