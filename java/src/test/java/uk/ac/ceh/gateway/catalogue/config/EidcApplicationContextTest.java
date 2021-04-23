package uk.ac.ceh.gateway.catalogue.config;

import freemarker.template.Configuration;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.CatalogueWebTest;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubService;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadDocumentService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles({"auth:crowd", "upload:hubbub"})
@CatalogueWebTest
@DisplayName("EIDC production context")
class EidcApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Some critical beans configured")
    void testContext() {
        assertNotNull(applicationContext.getBean("catalogueService"));
        assertNotNull(applicationContext.getBean("codeLookupService"));
        assertNotNull(applicationContext.getBean("dataciteService"));
        assertNotNull(applicationContext.getBean("jenaLookupService"));
        assertNotNull(applicationContext.getBean("permission"));
    }

    @Test
    @DisplayName("Hubbub configured correctly, Simple Upload controller not created")
    void hubbubUploadBeansPresent() {
        assertNotNull(applicationContext.getBean(UploadController.class));
        assertNotNull(applicationContext.getBean(UploadDocumentService.class));
        assertNotNull(applicationContext.getBean(HubbubService.class));
        Assertions.assertThrows(NoSuchBeanDefinitionException.class, () ->
            applicationContext.getBean(uk.ac.ceh.gateway.catalogue.upload.simple.UploadController.class)
        );
    }

    @Test
    @DisplayName("Freemarker shared variables present")
    void freemarkerConfiguredCorrectly() {
        val freemarkerConfiguration = (Configuration) applicationContext.getBean(freemarker.template.Configuration.class);
        assertNotNull(freemarkerConfiguration);
        assertNotNull(freemarkerConfiguration.getSharedVariable("catalogues"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("codes"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("downloadOrderDetails"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("geminiHelper"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("jena"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("jira"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("mapServerDetails"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("metadataQuality"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("permission"));
    }
}
