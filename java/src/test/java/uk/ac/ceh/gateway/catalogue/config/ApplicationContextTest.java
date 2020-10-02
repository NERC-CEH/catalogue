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

@ActiveProfiles("production")
@TestPropertySource
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ApplicationContextTest {
    // Check the production application context can be created and everything wired up

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testContext() {
        assertNotNull(applicationContext.getBean("permission"));
    }
}
