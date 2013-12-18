package uk.ac.ceh.ukeof.model.simple;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@XmlRootElement
@XmlType(propOrder = {
    "observingCapabilities",
    "supersedes",
    "supersededBy",
    "narrowerThan",
    "broaderThan",
    "involvedIn",
    "operationalPeriod",
    "measurementRegime",
    "resultAquisitionSources",
    "mobile",
    "geometry",
    "belongsTo",
    "relatedTo"
})
public class Facility extends BaseMonitoringType {
    
    private List<Link> 
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
    
    private Link measurementRegime;
    
    @XmlElement(name = "resultAquisitionSource")
    private List<Link> resultAquisitionSources  = new ArrayList<>();
    
    private String mobile;
    
    private Geometry geometry;
    
    @Data
    public static class Geometry {
        @XmlAttribute(name = "SRS")
        private final String SRS = "urn:ogc:def:crs:EPSG::4326";
        
        @XmlValue
        private String value;
    }
}