package uk.ac.ceh.gateway.catalogue.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotNull;

@ActiveProfiles("auth:datalabs")
@TestPropertySource
@ContextConfiguration(classes = {DataLabsApplicationContextTest.class, WebConfig.class})
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
public class DataLabsApplicationContextTest {
    // Check the production application context can be created and everything wired up

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void authenticationContext() {
        assertNotNull(applicationContext.getBean("dataLabsAuthenticationProvider"));
        assertNotNull(applicationContext.getBean("rememberMeServicesDataLabs"));
        assertNotNull(applicationContext.getBean("dataLabsGroupStore"));
        assertNotNull(applicationContext.getBean("rememberMeAuthenticationFilter"));
    }

}



