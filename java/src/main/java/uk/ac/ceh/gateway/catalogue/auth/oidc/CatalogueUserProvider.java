package uk.ac.ceh.gateway.catalogue.auth.oidc;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public interface CatalogueUserProvider {
    CatalogueUser provide(String token);
}
