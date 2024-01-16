package uk.ac.ceh.gateway.catalogue.ef;

import javax.xml.bind.annotation.XmlType;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@XmlType(propOrder = {
    "observingTime",
    "processType",
    "resultNature",
    "onlineResource",
    "domain",
    "phenomenon",
    "process"
})
public class ObservingCapability {
    private Link
        domain,
        phenomenon,
        process,
        onlineResource,
        resultNature,
        processType;

    private Lifespan observingTime;
}
