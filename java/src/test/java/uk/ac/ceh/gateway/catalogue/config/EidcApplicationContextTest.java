package uk.ac.ceh.gateway.catalogue.config;

import freemarker.template.Configuration;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubService;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadDocumentService;

import static org.junit.Assert.assertNotNull;

@ActiveProfiles({"auth:crowd", "upload:hubbub"})
@TestPropertySource
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
public class EidcApplicationContextTest {
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
    public void hubbubUploadBeansPresent() {
        Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> {
            assertNotNull(applicationContext.getBean(UploadController.class));
            assertNotNull(applicationContext.getBean(UploadDocumentService.class));
            assertNotNull(applicationContext.getBean(HubbubService.class));
            // No uploadSimple.UploadController has been created, will throw NoSuchBeanDefinitionException
            applicationContext.getBean(uk.ac.ceh.gateway.catalogue.upload.simple.UploadController.class);
        });
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
}
