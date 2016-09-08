package uk.ac.ceh.gateway.catalogue.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueException;

public class InMemoryCatalogueService implements CatalogueService {
    private final Map<String, Catalogue> catalogues;
    private final String defaultCatalogueKey;

    public InMemoryCatalogueService(
        @NonNull String defaultCatalogueKey,
        Catalogue... catalogues
    ) {
        this.catalogues = new HashMap<>(catalogues.length);
        for (Catalogue catalogue : catalogues) {
            this.catalogues.put(catalogue.getId(), catalogue);
        }
        this.defaultCatalogueKey = defaultCatalogueKey;
    }

    @Override
    public Catalogue retrieve(String key) {
        try {
            return catalogues.get(key.toLowerCase());
        } catch (NullPointerException ex) {
            throw new CatalogueException(String.format("Could not retrieve catalogue for: %s", key), ex);
        }
    }

    @Override
    public Catalogue defaultCatalogue() {
        return retrieve(defaultCatalogueKey);
    }

    @Override
    public List<Catalogue> retrieveAll() {
        return (List<Catalogue>) catalogues.values();
    }

}
