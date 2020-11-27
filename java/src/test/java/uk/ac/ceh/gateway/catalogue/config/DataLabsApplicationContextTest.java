package uk.ac.ceh.gateway.catalogue.config;

import freemarker.template.Configuration;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.RememberMeServices;
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
        assertNotNull(applicationContext.getBean("catalogueService"));
        assertNotNull(applicationContext.getBean("codeNameLookupService"));
        assertNotNull(applicationContext.getBean("dataciteService"));
        assertNotNull(applicationContext.getBean("jenaLookupService"));
        assertNotNull(applicationContext.getBean("permission"));
        assertNotNull(applicationContext.getBean(RememberMeServices.class));
        assertNotNull(applicationContext.getBean(AuthenticationProvider.class));
    }

    @Test
    public void freemarkerConfiguredCorrectly() {
        val freemarkerConfiguration = (Configuration) applicationContext.getBean("freemarkerConfiguration");
        assertNotNull(
                "Freemarker configuration not found",
                freemarkerConfiguration
        );
        assertNotNull(
                "Freemarker missing shared variable: catalogue",
                freemarkerConfiguration.getSharedVariable("catalogues")
        );
        assertNotNull(
                "Freemarker missing shared variable: codes",
                freemarkerConfiguration.getSharedVariable("codes")
        );
        assertNotNull("Freemarker missing shared variable: downloadOrderDetails",
                freemarkerConfiguration.getSharedVariable("downloadOrderDetails")
        );
        assertNotNull(
                "Freemarker missing shared variable: geminiHelper",
                freemarkerConfiguration.getSharedVariable("geminiHelper")
        );
        assertNotNull(
                "Freemarker missing shared variable: jena",
                freemarkerConfiguration.getSharedVariable("jena")
        );
        assertNotNull(
                "Freemarker missing shared variable: jira",
                freemarkerConfiguration.getSharedVariable("jira")
        );
        assertNotNull(
                "Freemarker missing shared variable: mapServerDetails",
                freemarkerConfiguration.getSharedVariable("mapServerDetails")
        );
        assertNotNull(
                "Freemarker missing shared variable: metadataQuality",
                freemarkerConfiguration.getSharedVariable("metadataQuality")
        );
        assertNotNull(
                "Freemarker missing shared variable: permission",
                freemarkerConfiguration.getSharedVariable("permission")
        );
    }

    @org.springframework.context.annotation.Configuration
    public static class TestConfig {
        private List<String> userPermissions;

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
                // implement methods
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
                // implement methods
            };
        }
    }
}



