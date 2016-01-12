package uk.ac.ceh.gateway.catalogue.imp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class AssociationResource extends Link{
    private String associationType;
}
