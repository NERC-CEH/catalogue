package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
public class Parameter {
    private String name, definition, unitOfMeasure;
}
