package uk.ac.ceh.gateway.catalogue.publication;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public interface PublicationService {
    StateResource current(CatalogueUser user, String fileIdentifier);
    StateResource transition(CatalogueUser user, String fileIdentifier, String toState);
}
