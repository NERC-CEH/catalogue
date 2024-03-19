package uk.ac.ceh.gateway.catalogue.userdetails;

import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public class SecurityUserInfo implements UserInfo {

    /**
     * Check if the user is logged in and whether the user is public or not.
     * A public user will be in the AnonymousUserAuthenticationFilter so won't be logged in.
     *
     * @return Returns true if the user is logged in and not public, false otherwise.
     */
    @Override
    public boolean isLoggedIn() {
        val principal = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
        if (principal instanceof CatalogueUser user) {
            return !user.isPublic();
        }
        return false;
    }
}
