package uk.ac.ceh.gateway.catalogue.services;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.State;

public interface PublicationService {
    State current(CatalogueUser user, String file);
    void transition(CatalogueUser user, String file, String state);
}