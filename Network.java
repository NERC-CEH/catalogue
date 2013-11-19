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
    "observingCapabilities",
    "supersedes",
    "supersededBy",
    "narrowerThan",
    "broaderThan",
    "involvedIn",
    "contains"
})
public class Network extends BaseMonitoringType {
    private List<Link> 
        involvedIn = new ArrayList<>(),
        supersedes = new ArrayList<>(),
        supersededBy = new ArrayList<>();
    
    private List<Link.TimedLink>
        broaderThan = new ArrayList<>(),
        narrowerThan = new ArrayList<>(),
        contains = new ArrayList<>();
    
    @XmlElement(name = "observingCapability")
    private List<Facility.ObservingCapability> 
        observingCapabilities = new ArrayList<>();
}