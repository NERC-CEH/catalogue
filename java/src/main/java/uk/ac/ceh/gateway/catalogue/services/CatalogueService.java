package uk.ac.ceh.gateway.catalogue.services;

import uk.ac.ceh.gateway.catalogue.model.Catalogue;

public interface CatalogueService {
    Catalogue retrieve(String hostname);
}
