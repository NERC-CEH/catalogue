package uk.ac.ceh.gateway.catalogue.services;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;

public interface PublicationService {
    StateResource current(CatalogueUser user, String fileIdentifier);
    StateResource transition(CatalogueUser user, String fileIdentifier, String toState);
}