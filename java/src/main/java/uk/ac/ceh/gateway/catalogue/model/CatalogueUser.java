package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;
import lombok.experimental.Accessors;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;
import static uk.ac.ceh.components.userstore.UserAttribute.*;

@Data
@Accessors(chain = true)
public class CatalogueUser implements User, DataAuthor {
    public static CatalogueUser PUBLIC_USER = new CatalogueUser();
    
    private @UserAttribute(USERNAME) String username;
    private @UserAttribute(EMAIL) String email;
    
    public boolean isPublic() {
        return username == null;
    } 
}
