package uk.ac.ceh.gateway.catalogue.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotNull;

@ActiveProfiles("auth:datalabs")
@TestPropertySource
@ContextConfiguration(classes = {DataLabsApplicationContextTest.class, WebConfig.class})
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
        assertNotNull(applicationContext.getBean("dataLabsGroupStore"));
    }

}



