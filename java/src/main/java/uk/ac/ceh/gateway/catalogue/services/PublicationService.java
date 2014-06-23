package uk.ac.ceh.gateway.catalogue.services;

import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;

public interface PublicationService {
    StateResource current(CatalogueUser user, String fileIdentifier, UriComponentsBuilder builder);
    StateResource transition(CatalogueUser user, String fileIdentifier, String toState, UriComponentsBuilder builder);
}