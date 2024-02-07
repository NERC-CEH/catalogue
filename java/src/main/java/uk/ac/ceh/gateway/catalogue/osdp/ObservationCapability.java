package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import uk.ac.ceh.gateway.catalogue.model.Link;

//TODO this should be deprecate and removed as OSDP no longer used
@Data
public class ObservationCapability {
    private String observingTime;
    private Link observedPropertyName,
        observedPropertyUnitOfMeasure,
        procedureName;
}
