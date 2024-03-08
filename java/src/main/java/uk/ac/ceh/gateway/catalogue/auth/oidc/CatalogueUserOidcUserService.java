package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@Slf4j
public class CatalogueUserOidcUserService extends OidcUserService {

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.info(userRequest.getIdToken().getSubject());
        return new CatalogueUser(super.loadUser(userRequest));
    }
}
