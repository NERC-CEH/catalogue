package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import uk.ac.ceh.components.userstore.inmemory.InMemoryGroupStore;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
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
    public static final String READONLY_ROLE     = "ROLE_CIG_READONLY";
    public static final String EDITOR_ROLE      = DocumentController.EDITOR_ROLE;
    public static final String PUBLISHER_ROLE   = DocumentController.PUBLISHER_ROLE;
    public static final String MAINTENANCE_ROLE = DocumentController.MAINTENANCE_ROLE;
    
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
        CatalogueUser bamboo =  new CatalogueUser()
                                        .setUsername("readonly")
                                        .setEmail("readonly@ceh.ac.uk");
        
        groupStore().grantGroupToUser(bamboo, READONLY_ROLE);
        userStore().addUser(bamboo, "readonlypassword");
        return bamboo;
    }
    
    @Bean @Qualifier("editor")
    public CatalogueUser editor() throws UsernameAlreadyTakenException {
        CatalogueUser bamboo =  new CatalogueUser()
                                        .setUsername("editor")
                                        .setEmail("editor@ceh.ac.uk");
        
        groupStore().grantGroupToUser(bamboo, EDITOR_ROLE);
        groupStore().grantGroupToUser(bamboo, CEH_GROUP_NAME);
        userStore().addUser(bamboo, "editorpassword");
        return bamboo;
    }
    
    @Bean @Qualifier("publisher")
    public CatalogueUser publisher() throws UsernameAlreadyTakenException {
        CatalogueUser bamboo =  new CatalogueUser()
                                        .setUsername("publisher")
                                        .setEmail("publisher@ceh.ac.uk");
        
        groupStore().grantGroupToUser(bamboo, PUBLISHER_ROLE);
        userStore().addUser(bamboo, "publisherpassword");
        return bamboo;
    }
    
    @Bean @Qualifier("admin")
    public CatalogueUser admin() throws UsernameAlreadyTakenException {
        CatalogueUser bamboo =  new CatalogueUser()
                                        .setUsername("admin")
                                        .setEmail("admin@ceh.ac.uk");
        
        groupStore().grantGroupToUser(bamboo, MAINTENANCE_ROLE);
        userStore().addUser(bamboo, "adminpassword");
        return bamboo;
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
        return toReturn;
    }
    
    @Bean
    public InMemoryUserStore<CatalogueUser> userStore() {
        return new InMemoryUserStore<>();
    }
}
