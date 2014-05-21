package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.userstore.UserAttribute;
import static uk.ac.ceh.components.userstore.UserAttribute.*;

/**
 *
 * @author cjohn
 */
@Data
public class User implements uk.ac.ceh.components.userstore.User, DataAuthor {
    public static User PUBLIC_USER = new User();
    
    private @UserAttribute(USERNAME) String username;
    private @UserAttribute(EMAIL) String email;
    
    public boolean isPublic() {
        return username == null;
    } 
}
