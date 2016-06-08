package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import uk.ac.ceh.components.userstore.inmemory.InMemoryGroupStore;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.controllers.DataciteController;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

/**
 * The following spring JavaConfig defines the beans required for the interacting
 * with a in memory user store. This is useful when developing the application
 * in an environment which can not contact Crowd
 * @see CrowdUserStoreConfig
 * @author cjohn
 */
@Configuration
@Profile("development")
public class DevelopmentUserStoreConfig {
    public static final String CEH_GROUP_NAME   = "CEH";
    public static final String READONLY_ROLE    = "ROLE_CIG_READONLY";
    public static final String EDITOR_ROLE      = DocumentController.EDITOR_ROLE;
    public static final String PUBLISHER_ROLE   = DocumentController.PUBLISHER_ROLE;
    public static final String MAINTENANCE_ROLE = DocumentController.MAINTENANCE_ROLE;
    public static final String DATACITE_ROLE    = DataciteController.DATACITE_ROLE;
    
    @Bean @Qualifier("superadmin")
    public CatalogueUser superadmin() throws UsernameAlreadyTakenException {
        CatalogueUser superadmin = new CatalogueUser()
                                        .setUsername("superadmin")
                                        .setEmail("superadmin@ceh.ac.uk");
        
        groupStore().grantGroupToUser(superadmin, CEH_GROUP_NAME);
        groupStore().grantGroupToUser(superadmin, EDITOR_ROLE);
        groupStore().grantGroupToUser(superadmin, PUBLISHER_ROLE);
        groupStore().grantGroupToUser(superadmin, MAINTENANCE_ROLE);
        groupStore().grantGroupToUser(superadmin, DATACITE_ROLE);
        userStore().addUser(superadmin, "superadminpassword");
        return superadmin;
    }
    
    @Bean @Qualifier("bamboo")
    public CatalogueUser bamboo() throws UsernameAlreadyTakenException {
        CatalogueUser bamboo =  new CatalogueUser()
                                        .setUsername("bamboo")
                                        .setEmail("bamboo@ceh.ac.uk");
        
        groupStore().grantGroupToUser(bamboo, CEH_GROUP_NAME);
        userStore().addUser(bamboo, "bamboopassword");
        return bamboo;
    }
        
    @Bean @Qualifier("readonly")
    public CatalogueUser readonly() throws UsernameAlreadyTakenException {
        CatalogueUser readonly =  new CatalogueUser()
                                        .setUsername("readonly")
                                        .setEmail("readonly@ceh.ac.uk");
        
        groupStore().grantGroupToUser(readonly, READONLY_ROLE);
        userStore().addUser(readonly, "readonlypassword");
        return readonly;
    }
    
    @Bean @Qualifier("editor")
    public CatalogueUser editor() throws UsernameAlreadyTakenException {
        CatalogueUser editor =  new CatalogueUser()
                                        .setUsername("editor")
                                        .setEmail("editor@ceh.ac.uk");
        
        groupStore().grantGroupToUser(editor, EDITOR_ROLE);
        groupStore().grantGroupToUser(editor, CEH_GROUP_NAME);
        userStore().addUser(editor, "editorpassword");
        return editor;
    }
    
    @Bean @Qualifier("publisher")
    public CatalogueUser publisher() throws UsernameAlreadyTakenException {
        CatalogueUser publisher =  new CatalogueUser()
                                        .setUsername("publisher")
                                        .setEmail("publisher@ceh.ac.uk");
        
        groupStore().grantGroupToUser(publisher, PUBLISHER_ROLE);
        userStore().addUser(publisher, "publisherpassword");
        return publisher;
    }
    
    @Bean @Qualifier("admin")
    public CatalogueUser admin() throws UsernameAlreadyTakenException {
        CatalogueUser admin =  new CatalogueUser()
                                        .setUsername("admin")
                                        .setEmail("admin@ceh.ac.uk");
        
        groupStore().grantGroupToUser(admin, MAINTENANCE_ROLE);
        userStore().addUser(admin, "adminpassword");
        return admin;
    }
    
    @Bean
    public InMemoryGroupStore<CatalogueUser> groupStore() {
        InMemoryGroupStore<CatalogueUser> toReturn = new InMemoryGroupStore<>();
        //create groups
        toReturn.createGroup(CEH_GROUP_NAME,   "Centre for Ecology & Hydrology");
        toReturn.createGroup(READONLY_ROLE,    "Read only role");
        toReturn.createGroup(EDITOR_ROLE,      "Editor Role");
        toReturn.createGroup(PUBLISHER_ROLE,   "Publisher Role");
        toReturn.createGroup(MAINTENANCE_ROLE, "System Admin Role");
        toReturn.createGroup(DATACITE_ROLE,    "Datacite Role");
        return toReturn;
    }
    
    @Bean
    public InMemoryUserStore<CatalogueUser> userStore() {
        return new InMemoryUserStore<>();
    }
}
