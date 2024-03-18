package uk.ac.ceh.gateway.catalogue.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;

import java.io.Serial;
import java.io.Serializable;

import static uk.ac.ceh.components.userstore.UserAttribute.EMAIL;
import static uk.ac.ceh.components.userstore.UserAttribute.USERNAME;

@Data
@EqualsAndHashCode(of="username")
public class CatalogueUser implements User, DataAuthor, Serializable {
    @Serial
    private static final long serialVersionUID = 42L;
    public static CatalogueUser PUBLIC_USER = new CatalogueUser();

    public CatalogueUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    /**
     * This constructor is only here to allow the @UserAttribute annotations to work
     */
    public CatalogueUser() {
        this(null, null);
    }

    @Setter(AccessLevel.NONE)
    private @UserAttribute(USERNAME) String username;
    @Setter(AccessLevel.NONE)
    private @UserAttribute(EMAIL) String email;

    public boolean isPublic() {
        return this.equals(PUBLIC_USER);
    }
}
