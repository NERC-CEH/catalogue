package uk.ac.ceh.ukeof.model.simple;

import java.util.*;
import javax.xml.bind.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

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
    "measurementRegime",
    "resultAcquisitionSources",
    "mobile",
    "geometry",
    "belongsTo",
    "relatedTo"
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
    
    private Link measurementRegime, facilityType;
    
    @XmlElement(name = "resultAcquisitionSource")
    private List<Link> resultAcquisitionSources  = new ArrayList<>();
    
    private String mobile;
    
    private Geometry geometry;
}