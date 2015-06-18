package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.components.userstore.crowd.CrowdApplicationCredentials;
import uk.ac.ceh.components.userstore.crowd.CrowdGroupStore;
import uk.ac.ceh.components.userstore.crowd.CrowdUserStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

/**
 * The following spring JavaConfig defines the beans required for the interacting
 * with a crowd userstore
 * @author cjohn
 */
@Configuration
@Profile("production")
public class CrowdUserStoreConfig {
    @Value("${userstore.crowd.address}") String address;
    @Value("${userstore.crowd.username}") String username;
    @Value("${userstore.crowd.password}") String password;
    
    @Autowired AnnotatedUserHelper<CatalogueUser> phantomUserBuilderFactory;
    
    @Bean
    public GroupStore<CatalogueUser> groupStore() {
        return new CrowdGroupStore<>(crowdCredentials());
    }
    
    @Bean
    public UserStore<CatalogueUser> userStore() {
        return new CrowdUserStore<>(crowdCredentials(), 
                                    phantomUserBuilderFactory, 
                                    phantomUserBuilderFactory);
    }
    
    @Bean
    public CrowdApplicationCredentials crowdCredentials() {
        return new CrowdApplicationCredentials(address, username, password);
    }
}
