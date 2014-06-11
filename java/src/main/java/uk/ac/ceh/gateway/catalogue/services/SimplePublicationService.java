package uk.ac.ceh.gateway.catalogue.services;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.State;

public class SimplePublicationService implements PublicationService {

    @Override
    public State current(CatalogueUser user, String file) {
        return State.builder().build();
    }

    @Override
    public State transition(CatalogueUser user, String file, String state) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}