package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
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

@Configuration
@Profile("production")
@Slf4j
public class CrowdUserStoreConfig {
    @Value("${crowd.address}") String address;
    @Value("${crowd.username}") String username;
    @Value("${crowd.password}") String password;
    
    @Autowired AnnotatedUserHelper<CatalogueUser> phantomUserBuilderFactory;
    
    @Bean
    public GroupStore<CatalogueUser> groupStore() {
        log.info("Creating CrowdGroupStore(address={}, username={}", address, username);
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
