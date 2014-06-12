package uk.ac.ceh.gateway.catalogue.services;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.State;

public interface StateAssembler {
    State toResource(CatalogueUser user, String filename, String state);
}