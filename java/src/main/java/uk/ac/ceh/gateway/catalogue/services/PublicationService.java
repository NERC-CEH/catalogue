package uk.ac.ceh.gateway.catalogue.services;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.State;

public interface PublicationService {
    State current(CatalogueUser user, String file);
    State transition(CatalogueUser user, String file, String state);
}