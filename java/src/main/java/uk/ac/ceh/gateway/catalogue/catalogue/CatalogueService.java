package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.NonNull;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;

import java.util.List;

public interface CatalogueService {
    Catalogue retrieve(@NonNull String key);
    Catalogue defaultCatalogue();
    List<Catalogue> retrieveAll();
}
