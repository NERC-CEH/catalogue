package uk.ac.ceh.gateway.catalogue.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.Groups;
import uk.ac.ceh.gateway.catalogue.model.User;

/**
 *
 * @author cjohn
 */
@Service
public class UserMembershipService {
    public static final String MANAGEMENT_GROUP_ROLE_NAME = "ROLE_MANAGEMENT";
    public static final String MANAGEMENT_ADMIN_ROLE_NAME = "ROLE_ADMIN";
    public static final String USER_ROLE_NAME = "ROLE_USER";
    
    private final GroupStore<User> groupstore;
    
    @Autowired
    public UserMembershipService(GroupStore<User> groupstore) {
        this.groupstore = groupstore;
    }
    
    public UserMembership getMembership(User user) {
        if(user.isPublic()) {
            return new UserMembership(user, Collections.EMPTY_LIST);
        }
        else {
            List<Group> groups = groupstore.getGroups(user);
            return new UserMembership(user, groups);
        }
    }
    
    public static class UserMembership extends Groups {
        private final User user;
        
        private UserMembership(User user, List<Group> groups) {
            super(groups);
            this.user = user;
        }
        
        public User getUser() {
            return user;
        }
        
        public boolean isAuthenticated() {
            return getRoleNames().contains(USER_ROLE_NAME);
        }
        
        public boolean isInOrganisation(String organistation) {
            return getOrganisationNames().contains(organistation);
        }
        
        public boolean isUserAllowedToWriteAllDocuments() {
            return getRoleNames().contains(MANAGEMENT_ADMIN_ROLE_NAME);
        }
        
        public boolean isAllowedToWriteDocumentFor(String organisation) {
            return isUserAllowedToWriteAllDocuments() || isInOrganisation(organisation);
        }

        public boolean isUserAllowedToReadAllDocuments() {
            Collection<String> roleNames = getRoleNames();
            return roleNames.contains(MANAGEMENT_GROUP_ROLE_NAME) || roleNames.contains(MANAGEMENT_ADMIN_ROLE_NAME);
        }
    }
}
