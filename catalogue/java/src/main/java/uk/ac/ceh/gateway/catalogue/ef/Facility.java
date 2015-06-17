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
    "facilityType",
    "operationalPeriod",
    "resultAcquisitionSources",
    "mobile",
    "geometry",
    "belongsTo",
    "relatedTo"
})
@ConvertUsing({
    @Template(called="html/ef.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="xml/emf.xml.tpl",   whenRequestedAs=EF_INSPIRE_XML_VALUE)
})
public class Facility extends BaseMonitoringType {
    
    private List<Link> 
        legalBackground = new ArrayList<>(),
        supersedes  = new ArrayList<>(), 
        supersededBy = new ArrayList<>(),
        involvedIn = new ArrayList<>(),
        relatedTo = new ArrayList<>();
    
    private List<Lifespan> operationalPeriod;
    
    private List<Link.TimedLink>
        narrowerThan  = new ArrayList<>(),
        broaderThan = new ArrayList<>(),
        belongsTo = new ArrayList<>();
    
    @XmlElement(name = "observingCapability")
    private List<ObservingCapability> observingCapabilities  = new ArrayList<>();
    
    private Link facilityType;
    
    @XmlElement(name = "resultAcquisitionSource")
    private List<Link> resultAcquisitionSources  = new ArrayList<>();
    
    private String mobile;
    
    private Geometry geometry;
}