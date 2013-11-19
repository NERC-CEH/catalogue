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

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@XmlRootElement
@XmlType(propOrder = {
    "lifespan",
    "parametersMeasured",
    "environmentalDomains",
    "reasonsForCollection",
    "frequencyOfObservations",
    "setUpFor",
    "uses"
})
public class Activity extends BaseMonitoringType {
    
    private Lifespan lifespan;
    
    private List<Keyword> parametersMeasured = new ArrayList<>();
    
    @XmlElement(name = "environmentalDomain")
    private List<CodeList> environmentalDomains = new ArrayList<>();
    
    @XmlElement(name = "reasonForCollection")
    private List<CodeList> reasonsForCollection = new ArrayList<>();
    
    private String frequencyOfObservations;
    
    private List<Link> 
        setUpFor = new ArrayList<>(),
        uses = new ArrayList<>();
}