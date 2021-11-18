package uk.ac.ceh.gateway.catalogue.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.CatalogueWebTest;
import uk.ac.ceh.gateway.catalogue.upload.simple.UploadController;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles({"auth:crowd", "upload:simple", "server:elter", "search:basic"})
@CatalogueWebTest
@DisplayName("Non EIDC production context")
class NonEidcApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Simple Uploader present not Hubbub")
    void hubbubUploadBeansPresent() {
        assertNotNull(applicationContext.getBean(UploadController.class));
        Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> {
            applicationContext.getBean(uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController.class);
        });
    }
}
