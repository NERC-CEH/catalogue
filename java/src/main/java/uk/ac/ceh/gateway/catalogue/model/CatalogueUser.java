package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;

import java.io.Serializable;

import static uk.ac.ceh.components.userstore.UserAttribute.*;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(of="username")
public class CatalogueUser implements User, DataAuthor, Serializable {
    static final long serialVersionUID = 42L;
    public static CatalogueUser PUBLIC_USER = new CatalogueUser();

    private @UserAttribute(USERNAME) String username;
    private @UserAttribute(EMAIL) String email;

    public boolean isPublic() {
        return username == null;
    }
}
