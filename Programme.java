package uk.ac.ceh.ukeof.model.simple;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@XmlRootElement
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@XmlType(propOrder = {
    "observingCapabilities",
    "supersedes",
    "supersededBy",
    "narrowerThan",
    "broaderThan",
    "lifespan",
    "environmentalDomains",
    "legislation",
    "triggers"
})
public class Programme extends BaseMonitoringType {
    
    private Lifespan 
        lifespan;
    
    @XmlElement(name = "environmentalDomain")
    private List<CodeList> 
        environmentalDomains = new ArrayList<>();
    
    private List<Link> 
        supersedes = new ArrayList<>(),
        supersededBy = new ArrayList<>(),
        triggers = new ArrayList<>();
    
    private List<Link.TimedLink> 
        broaderThan  = new ArrayList<>(),
        narrowerThan = new ArrayList<>();

    private List<Legislation> 
        legislation = new ArrayList<>();
    
    @XmlElement(name = "observingCapability")
    private List<Facility.ObservingCapability>
        observingCapabilities  = new ArrayList<>();
}