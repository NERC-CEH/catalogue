package uk.ac.ceh.gateway.catalogue.services;

import lombok.NonNull;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;

public interface CatalogueService {
    Catalogue retrieve(@NonNull String key);
}
