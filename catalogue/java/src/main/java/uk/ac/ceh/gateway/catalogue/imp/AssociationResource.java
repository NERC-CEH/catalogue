package uk.ac.ceh.gateway.catalogue.imp;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class AssociationResource extends Link{
    private String associationType;
}
