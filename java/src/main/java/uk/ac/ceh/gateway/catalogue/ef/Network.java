package uk.ac.ceh.gateway.catalogue.ef;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import javax.xml.bind.annotation.XmlElement;
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
    @Template(called="html/ef.ftl", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
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