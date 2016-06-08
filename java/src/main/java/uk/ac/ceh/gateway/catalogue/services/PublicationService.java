package uk.ac.ceh.gateway.catalogue.services;

import java.net.URI;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;

public interface PublicationService {
    StateResource current(CatalogueUser user, String fileIdentifier, UriComponentsBuilder builder, URI metadataUrl);
    StateResource transition(CatalogueUser user, String fileIdentifier, String toState, UriComponentsBuilder builder, URI metadataUrl);
}