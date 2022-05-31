package uk.ac.ceh.gateway.catalogue.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.ac.ceh.components.userstore.Group;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CatalogueGroup implements Group, Serializable {
    static final long serialVersionUID = 42L;
    private String name;

    @Override
    public String getDescription() {
        return name;
    }
}
