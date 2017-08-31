package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;

@Data
public class ObservationCapability {
    private String observingTime,
        observedPropertyName,
        observedPropertyDefinition,
        observedPropertyUnitOfMeasure,
        procedureName;
}
