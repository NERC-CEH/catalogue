package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import uk.ac.ceh.gateway.catalogue.model.Link;

@Data
public class ObservationCapability {
    private String observingTime;
    private Link observedPropertyName,
        observedPropertyUnitOfMeasure,
        procedureName;
}
