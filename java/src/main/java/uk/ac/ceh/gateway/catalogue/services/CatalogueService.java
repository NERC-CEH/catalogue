package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;
import lombok.NonNull;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;

public interface CatalogueService {
    Catalogue retrieve(@NonNull String key);
    Catalogue defaultCatalogue();
    List<Catalogue> retrieveAll();
}
