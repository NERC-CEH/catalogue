package uk.ac.ceh.gateway.catalogue.services;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.PublicationResource;

public interface PublicationService {
    PublicationResource current(CatalogueUser user, String fileIdentifier);
    PublicationResource transition(CatalogueUser user, String fileIdentifier, String toState);
}