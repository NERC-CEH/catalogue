package uk.ac.ceh.gateway.catalogue.auth.cognito;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Collection;

@Getter
public class CognitoAuthenticationToken extends AbstractAuthenticationToken {
    private final CatalogueUser user;
    private final Object credentials;
    public CognitoAuthenticationToken(CatalogueUser user, Collection<? extends GrantedAuthority> authorities, Object credentials) {
        super(authorities);
        this.user = user;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }
}
