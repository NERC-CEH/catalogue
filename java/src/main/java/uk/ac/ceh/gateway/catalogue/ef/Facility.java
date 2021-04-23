package uk.ac.ceh.gateway.catalogue.ef;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.indexing.WellKnownText;

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
    "facilityType",
    "operationalPeriod",
    "resultAcquisitionSources",
    "mobile",
    "geometry",
    "belongsTo",
    "relatedTo"
})
@ConvertUsing({
    @Template(called="html/ef.ftl", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="xml/emf.xml.tpl",   whenRequestedAs=EF_INSPIRE_XML_VALUE)
})
public class Facility extends BaseMonitoringType implements WellKnownText {
    
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

    @Override
    public List<String> getWKTs() {
        List<String> toReturn = super.getWKTs();
        if (geometry != null) {
            toReturn.add(geometry.getValue());
        }
        return toReturn;
    }
}