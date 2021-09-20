package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@ToString
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
        log.info("Creating {}", this);
    }

    @Override
    public Catalogue retrieve(@NonNull String key) {
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
        List<Catalogue> toReturn = new ArrayList<>(catalogues.values());
        Collections.sort(toReturn);
        return toReturn;
    }

}
