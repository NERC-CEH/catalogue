package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.NonNull;

import java.util.List;

public interface CatalogueService {
    String ALL_CATALOGUES_ID = "all";

    Catalogue retrieve(@NonNull String key);
    Catalogue defaultCatalogue();
    List<Catalogue> retrieveAll();
}
