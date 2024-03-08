package uk.ac.ceh.gateway.catalogue.model;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static uk.ac.ceh.components.userstore.UserAttribute.EMAIL;
import static uk.ac.ceh.components.userstore.UserAttribute.USERNAME;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(of="username")
public class CatalogueUser implements User, DataAuthor, OidcUser, Serializable {
    @Serial
    private static final long serialVersionUID = 42L;
    public static CatalogueUser PUBLIC_USER = new CatalogueUser();

    private @UserAttribute(USERNAME) String username;
    private @UserAttribute(EMAIL) String email;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private OidcUser oidcUser;

    public CatalogueUser() {
    }

    public CatalogueUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public CatalogueUser(OidcUser oidcUser) {
        this.username = oidcUser.getEmail();
        this.email = oidcUser.getEmail();
        this.oidcUser = oidcUser;
    }

    public boolean isPublic() {
        return username == null;
    }

    @Override
    public Map<String, Object> getClaims() {
        if (oidcUser != null) {
            return oidcUser.getClaims();
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public OidcUserInfo getUserInfo() {
        if (oidcUser != null) {
            return oidcUser.getUserInfo();
        } else {
            return null;
        }

    }

    @Override
    public OidcIdToken getIdToken() {
        if (oidcUser != null) {
            return oidcUser.getIdToken();
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        if (oidcUser != null) {
            return oidcUser.getAttributes();
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (oidcUser != null) {
            return oidcUser.getAuthorities();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getName() {
        if (oidcUser != null) {
            return oidcUser.getName();
        } else {
            return username;
        }
    }
}
