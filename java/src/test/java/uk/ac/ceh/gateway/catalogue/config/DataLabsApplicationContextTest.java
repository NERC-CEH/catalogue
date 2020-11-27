package uk.ac.ceh.gateway.catalogue.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ceh.components.userstore.*;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;

import static org.junit.Assert.assertNotNull;

@ActiveProfiles(profiles = "auth:datalabs")
@TestPropertySource
@ContextConfiguration(classes = {DataLabsApplicationContextTest.TestConfig.class, WebConfig.class})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class DataLabsApplicationContextTest {
    // Check the production application context can be created and everything wired up

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testContext() {
        assertNotNull(applicationContext.getBean("dataLabsAuthenticationProvider"));
        assertNotNull(applicationContext.getBean("rememberMeServicesDataLabs"));
    }

    @org.springframework.context.annotation.Configuration
    public static class TestConfig {

        @Bean
        public UserStore<CatalogueUser>  userStore() {
            return new UserStore() {
                @Override
                public User getUser(String username) throws UnknownUserException {
                    return null;
                }

                @Override
                public boolean userExists(String username) {
                    return false;
                }

                @Override
                public User authenticate(String username, String password) throws InvalidCredentialsException {
                    return null;
                }
            };
        }

        @Bean
        public GroupStore<CatalogueUser>  groupStore(){
            return new GroupStore() {
                @Override
                public List<Group> getGroups(User user) {
                    return null;
                }

                @Override
                public Group getGroup(String name) throws IllegalArgumentException {
                    return null;
                }

                @Override
                public List<Group> getAllGroups() {
                    return null;
                }

                @Override
                public boolean isGroupInExistance(String name) {
                    return false;
                }

                @Override
                public boolean isGroupDeletable(String group) throws IllegalArgumentException {
                    return false;
                }
            };
        }
    }
}



