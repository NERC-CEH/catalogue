package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import uk.ac.ceh.components.userstore.inmemory.InMemoryGroupStore;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
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
    public static final String CEH_GROUP_NAME = "CEH"; 
    
    @Bean @Qualifier("bamboo")
    public CatalogueUser bamboo() throws UsernameAlreadyTakenException {
        CatalogueUser bamboo =  new CatalogueUser()
                                        .setUsername("bamboo")
                                        .setEmail("bamboo@ceh.ac.uk");
        
        groupStore().grantGroupToUser(bamboo, CEH_GROUP_NAME);
        userStore().addUser(bamboo, "bamboopassword");
        return bamboo;
    } 
    
    @Bean
    public InMemoryGroupStore<CatalogueUser> groupStore() {
        InMemoryGroupStore<CatalogueUser> toReturn = new InMemoryGroupStore<>();
        //create groups
        toReturn.createGroup(CEH_GROUP_NAME, "Centre for Ecology & Hydrology");
        return toReturn;
    }
    
    @Bean
    public InMemoryUserStore<CatalogueUser> userStore() {
        return new InMemoryUserStore<>();
    }
}
