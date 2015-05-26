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
    "lifespan",
    "setUpFor",
    "uses"
})
public class Activity extends BaseMonitoringType {
    
    private Lifespan lifespan;
    
    private List<Link> 
        setUpFor = new ArrayList<>(),
        uses = new ArrayList<>();
}