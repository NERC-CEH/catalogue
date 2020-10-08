package uk.ac.ceh.gateway.catalogue.config;

import freemarker.template.Configuration;
import lombok.val;
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
        assertNotNull(applicationContext.getBean("catalogueService"));
        assertNotNull(applicationContext.getBean("codeNameLookupService"));
        assertNotNull(applicationContext.getBean("dataciteService"));
        assertNotNull(applicationContext.getBean("jenaLookupService"));
        assertNotNull(applicationContext.getBean("permission"));
    }
    
    @Test
    public void freemarkerConfiguredCorrectly() {
        val freemarkerConfiguration = (Configuration)applicationContext.getBean("freemarkerConfiguration");
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
}
